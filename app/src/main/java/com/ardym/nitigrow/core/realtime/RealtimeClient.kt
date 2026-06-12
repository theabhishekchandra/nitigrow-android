package com.ardym.nitigrow.core.realtime

import com.ardym.nitigrow.BuildConfig
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.data.remote.dto.MessageDto
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal Socket.io v4 (Engine.IO v4) client over OkHttp WebSocket.
 *
 * TODO(deps): Strongly prefer swapping this hand-rolled client for the official
 *   `io.socket:socket-io-client:2.1.0` (transitively `engine.io-client`). That
 *   was requested but cannot be done here because editing `build.gradle.kts`
 *   is out of scope for this change. Once the dependency is added, replace the
 *   frame handling below with `IO.socket(BASE_URL)` + `socket.on("new_message", …)`
 *   and emit `join_tenant` in the `Socket.EVENT_CONNECT` handler.
 *
 * Wire protocol implemented here (matches socket.io-client behaviour):
 *
 *   Engine.IO packet = "<engineType><payload>"
 *     0 = open, 2 = ping, 3 = pong, 4 = message
 *   Socket.IO packet (carried inside an Engine.IO "message", i.e. after a "4"):
 *     0 = CONNECT, 1 = DISCONNECT, 2 = EVENT, 3 = ACK, 4 = CONNECT_ERROR
 *
 * Handshake performed on connect:
 *   1. Server sends Engine.IO OPEN: `0{"sid":…,"pingInterval":…,"pingTimeout":…}`
 *   2. Client replies with Socket.IO CONNECT for the default namespace: `40`
 *   3. Server confirms namespace: `40{"sid":…}`
 *   4. Client emits `42["join_tenant","<accessToken>"]` to join `tenant-<id>` room.
 *
 * Server-side event names (see backend/src/appFactory.js + controllers):
 *   new_message, message_status, contact_typing, campaign_completed,
 *   campaign_paused, conversation_update, notification, window_expiring_soon.
 *
 * NOTE: the access token is sent in the `join_tenant` event body (the documented
 * mobile path), NOT as a `?token=` query param.
 */
@Singleton
class RealtimeClient @Inject constructor(
    private val httpClient: OkHttpClient,
    private val tokenStore: TokenDataStore,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    private val backoffSeq = listOf(1_000L, 2_000L, 5_000L, 10_000L, 30_000L)

    private val _events = MutableSharedFlow<RealtimeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    @Volatile private var manualDisconnect = false
    @Volatile private var attempt = 0

    /** Access token captured at connect time, replayed in the join_tenant emit. */
    @Volatile private var pendingToken: String = ""

    /** True once the socket.io namespace CONNECT ("40…") has been acknowledged. */
    @Volatile private var namespaceJoined = false

    fun connect() {
        if (socket != null) return
        manualDisconnect = false
        openSocket()
    }

    fun disconnect() {
        manualDisconnect = true
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        namespaceJoined = false
        // Polite socket.io DISCONNECT for the namespace before tearing the socket down.
        socket?.send("41")
        socket?.close(1000, "client_disconnect")
        socket = null
    }

    /**
     * Public API kept stable for repositories. The server keys typing by
     * `contactId`, so `conversationId` here is treated as the contactId.
     */
    fun emitTyping(conversationId: String, typing: Boolean) {
        val payload = mapOf("contactId" to conversationId, "typing" to typing)
        emit("contact_typing", payload)
    }

    private fun emit(event: String, payload: Any) {
        val data = gson.toJson(listOf(event, payload))
        // 4 = engine.io MESSAGE, 2 = socket.io EVENT → "42" prefix.
        socket?.send("42$data")
    }

    private fun openSocket() {
        pendingToken = runBlocking { tokenStore.accessTokenBlocking() }.orEmpty()
        namespaceJoined = false
        // No ?token= query param — auth happens via the join_tenant event.
        val url = BuildConfig.BASE_URL.replace("http", "ws")
            .replace("/api/", "/")
            .trimEnd('/') + "/socket.io/?EIO=4&transport=websocket"
        val req = Request.Builder().url(url).build()
        socket = httpClient.newWebSocket(req, listener)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            Timber.d("WS open")
            attempt = 0
            // Don't emit Connected yet — wait for the socket.io namespace ACK.
            startHeartbeat()
        }

        override fun onMessage(ws: WebSocket, text: String) {
            handleFrame(text)
        }

        override fun onClosing(ws: WebSocket, code: Int, reason: String) {
            ws.close(1000, null)
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            Timber.d("WS closed code=$code reason=$reason")
            socket = null
            namespaceJoined = false
            scope.launch { _events.emit(RealtimeEvent.Disconnected) }
            scheduleReconnect()
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Timber.w(t, "WS failure")
            socket = null
            namespaceJoined = false
            scope.launch { _events.emit(RealtimeEvent.Disconnected) }
            scheduleReconnect()
        }
    }

    private fun handleFrame(raw: String) {
        if (raw.isEmpty()) return
        when (raw[0]) {
            // Engine.IO OPEN — complete the socket.io handshake by joining the
            // default namespace. Payload (sid/pingInterval) is ignored; we use a
            // fixed 25s heartbeat which is safely below the server's pingTimeout.
            '0' -> socket?.send("40")
            // Engine.IO PING → reply PONG.
            '2' -> socket?.send("3")
            // Engine.IO MESSAGE — carries a socket.io packet.
            '4' -> handleSocketIoPacket(raw.substring(1))
        }
    }

    private fun handleSocketIoPacket(sio: String) {
        if (sio.isEmpty()) return
        when (sio[0]) {
            // Socket.IO CONNECT ack for the namespace → now safe to join the tenant room.
            '0' -> onNamespaceConnected()
            // Socket.IO EVENT.
            '2' -> parseEvent(sio.substring(1))
            // Socket.IO CONNECT_ERROR — server rejected the namespace/auth.
            '4' -> Timber.w("Socket.IO connect error: ${sio.substring(1)}")
            else -> { /* DISCONNECT / ACK / BINARY — not needed */ }
        }
    }

    private fun onNamespaceConnected() {
        if (namespaceJoined) return
        namespaceJoined = true
        // Auth + room join: emit ["join_tenant", "<accessToken>"].
        emit("join_tenant", pendingToken)
        scope.launch { _events.emit(RealtimeEvent.Connected) }
    }

    private fun parseEvent(json: String) {
        try {
            val arr = JsonParser.parseString(json).asJsonArray
            if (arr.size() == 0) return
            val name = arr[0].asString
            val payload: JsonElement? = if (arr.size() > 1) arr[1] else null
            val event: RealtimeEvent = when (name) {
                "new_message" -> {
                    val obj = payload?.asJsonObject ?: return
                    RealtimeEvent.NewMessage(parseNewMessage(obj))
                }
                "message_status" -> {
                    val obj = payload?.asJsonObject ?: return
                    // Server emits { waMessageId, status }. Repos key local rows by
                    // server message id; waMessageId is the only id available here,
                    // so it is forwarded as messageId. See follow-up note.
                    RealtimeEvent.StatusUpdate(
                        messageId = obj.stringOrNull("waMessageId")
                            ?: obj.stringOrNull("messageId")
                            ?: return,
                        status = obj.stringOrNull("status") ?: return
                    )
                }
                "contact_typing" -> {
                    val obj = payload?.asJsonObject ?: return
                    // Server emits { contactId } (presence of event == typing=true).
                    RealtimeEvent.TypingChange(
                        conversationId = obj.stringOrNull("contactId") ?: return,
                        typing = obj.boolOrNull("typing") ?: true
                    )
                }
                "campaign_completed" -> {
                    val obj = payload?.asJsonObject ?: return
                    val stats = obj.getAsJsonObject("stats")
                    RealtimeEvent.CampaignProgress(
                        campaignId = obj.stringOrNull("campaignId") ?: return,
                        status = "completed",
                        sent = stats.longOrZero("sent"),
                        delivered = stats.longOrZero("delivered"),
                        read = stats.longOrZero("read"),
                        failed = stats.longOrZero("failed")
                    )
                }
                "campaign_paused" -> {
                    val obj = payload?.asJsonObject ?: return
                    RealtimeEvent.CampaignProgress(
                        campaignId = obj.stringOrNull("campaignId") ?: return,
                        status = "paused",
                        sent = 0L,
                        delivered = 0L,
                        read = 0L,
                        failed = 0L
                    )
                }
                // The following events have no slot in the (other-vertical-owned)
                // RealtimeEvent contract yet, so they are logged and dropped rather
                // than dispatched. See follow-up note.
                "conversation_update",
                "notification",
                "window_expiring_soon" -> {
                    Timber.d("Unmapped realtime event '$name': $json")
                    return
                }
                else -> return
            }
            scope.launch { _events.emit(event) }
        } catch (t: Throwable) {
            Timber.w(t, "WS parse failed: $json")
        }
    }

    /**
     * Translate a server `new_message` payload into [MessageDto].
     *
     * Server shape:  { conversationId|contactId, contact?, message:<rawMongoMessage> }
     * where rawMongoMessage = { _id, contactId, direction, type, content (String|{text}),
     *                           status, createdAt, waMessageId }.
     *
     * [MessageDto] expects MongoDB-foreign field names (conversationId, text,
     * outbound, sentAt …). We build a normalized JsonObject and let Gson bind it,
     * so the existing `MessageDto.toEntity()` mapper keeps working unchanged.
     */
    private fun parseNewMessage(envelope: JsonObject): MessageDto {
        val msg: JsonObject =
            envelope.getAsJsonObjectOrNull("message") ?: envelope
        val contactId = envelope.stringOrNull("contactId")
            ?: envelope.stringOrNull("conversationId")
            ?: msg.stringOrNull("contactId")
            ?: ""

        val normalized = JsonObject().apply {
            addProperty("_id", msg.stringOrNull("_id") ?: msg.stringOrNull("waMessageId") ?: "")
            addProperty("conversationId", contactId)
            addProperty("text", extractText(msg))
            addProperty(
                "sentAt",
                msg.stringOrNull("createdAt") ?: msg.stringOrNull("sentAt") ?: ""
            )
            addProperty("outbound", msg.stringOrNull("direction") == "outbound")
            addProperty("status", msg.stringOrNull("status") ?: "delivered")
            addProperty("type", msg.stringOrNull("type") ?: "text")
            // clientId / media / replyTo are not provided on the realtime payload.
        }
        return gson.fromJson(normalized, MessageDto::class.java)
    }

    /** `content` may be a bare string or an object with a `.text` field. */
    private fun extractText(msg: JsonObject): String {
        val content = msg.get("content") ?: return ""
        return when {
            content.isJsonNull -> ""
            content.isJsonPrimitive -> content.asString
            content.isJsonObject -> content.asJsonObject.stringOrNull("text") ?: ""
            else -> ""
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (socket != null) {
                delay(25_000)
                socket?.send("2") // engine.io ping
            }
        }
    }

    private fun scheduleReconnect() {
        if (manualDisconnect) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = backoffSeq[attempt.coerceAtMost(backoffSeq.lastIndex)]
            attempt++
            delay(delayMs)
            openSocket()
        }
    }

    // ── JSON helpers ────────────────────────────────────────────────────────
    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString

    private fun JsonObject.boolOrNull(key: String): Boolean? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asBoolean

    private fun JsonObject?.longOrZero(key: String): Long =
        this?.get(key)?.takeIf { it.isJsonPrimitive }?.asLong ?: 0L

    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? =
        get(key)?.takeIf { it.isJsonObject }?.asJsonObject
}
