package com.ardym.nitigrow.core.realtime

import com.ardym.nitigrow.BuildConfig
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.data.remote.dto.MessageDto
import com.google.gson.Gson
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
 * Minimal Socket.io v4-compatible client over OkHttp WebSocket.
 *
 * Engine.io frame: "<type><payload>"
 *   0 = open, 2 = ping, 3 = pong, 4 = message
 * Socket.io frame inside engine.io message:
 *   0 = connect, 2 = event, 3 = ack, 4 = error
 *
 * Outbound event: 42["event_name", payloadObject]
 * Inbound event:  42["event_name", payloadObject]
 *
 * If backend uses raw WS instead of Socket.io, replace `parseFrame` + `emit`.
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

    fun connect() {
        if (socket != null) return
        manualDisconnect = false
        openSocket()
    }

    fun disconnect() {
        manualDisconnect = true
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        socket?.close(1000, "client_disconnect")
        socket = null
    }

    fun emitTyping(conversationId: String, typing: Boolean) {
        val payload = mapOf("conversationId" to conversationId, "typing" to typing)
        emit("typing", payload)
    }

    private fun emit(event: String, payload: Any) {
        val data = gson.toJson(listOf(event, payload))
        // 4 = engine.io message, 2 = socket.io event
        socket?.send("42$data")
    }

    private fun openSocket() {
        val token = runBlocking { tokenStore.accessTokenBlocking() }.orEmpty()
        val url = BuildConfig.BASE_URL.replace("http", "ws").trimEnd('/') +
            "/socket.io/?EIO=4&transport=websocket&token=$token"
        val req = Request.Builder().url(url).build()
        socket = httpClient.newWebSocket(req, listener)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            Timber.d("WS open")
            attempt = 0
            scope.launch { _events.emit(RealtimeEvent.Connected) }
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
            scope.launch { _events.emit(RealtimeEvent.Disconnected) }
            scheduleReconnect()
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Timber.w(t, "WS failure")
            socket = null
            scope.launch { _events.emit(RealtimeEvent.Disconnected) }
            scheduleReconnect()
        }
    }

    private fun handleFrame(raw: String) {
        if (raw.isEmpty()) return
        when (raw[0]) {
            '0' -> { /* engine.io open handshake */ }
            '2' -> socket?.send("3")    // pong
            '4' -> {
                val sio = raw.substring(1)
                if (sio.isEmpty()) return
                when (sio[0]) {
                    '0' -> { /* socket.io connect */ }
                    '2' -> parseEvent(sio.substring(1))
                    else -> { /* ignore */ }
                }
            }
        }
    }

    private fun parseEvent(json: String) {
        try {
            val arr = JsonParser.parseString(json).asJsonArray
            val name = arr[0].asString
            val payload = arr[1]
            val event: RealtimeEvent = when (name) {
                "message:new" -> RealtimeEvent.NewMessage(
                    gson.fromJson(payload, MessageDto::class.java)
                )
                "message:status" -> {
                    val obj = payload.asJsonObject
                    RealtimeEvent.StatusUpdate(
                        messageId = obj["messageId"].asString,
                        status = obj["status"].asString
                    )
                }
                "typing" -> {
                    val obj = payload.asJsonObject
                    RealtimeEvent.TypingChange(
                        conversationId = obj["conversationId"].asString,
                        typing = obj["typing"].asBoolean
                    )
                }
                "campaign:progress" -> {
                    val obj = payload.asJsonObject
                    RealtimeEvent.CampaignProgress(
                        campaignId = obj["campaignId"].asString,
                        status = obj["status"].asString,
                        sent = obj["sent"].asLong,
                        delivered = obj["delivered"].asLong,
                        read = obj["read"].asLong,
                        failed = obj["failed"].asLong
                    )
                }
                else -> return
            }
            scope.launch { _events.emit(event) }
        } catch (t: Throwable) {
            Timber.w(t, "WS parse failed: $json")
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
}
