package com.websbaba.nitigrow.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.ErrorType
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.realtime.RealtimeClient
import com.websbaba.nitigrow.core.realtime.RealtimeEvent
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.NitiGrowDatabase
import com.websbaba.nitigrow.data.local.dao.ConversationDao
import com.websbaba.nitigrow.data.local.dao.MessageDao
import com.websbaba.nitigrow.data.local.entity.MessageEntity
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.ChatApi
import com.websbaba.nitigrow.data.remote.api.InboxApi
import com.websbaba.nitigrow.data.remote.dto.SendMessageRequest
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val inboxApi: InboxApi,
    private val db: NitiGrowDatabase,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val realtime: RealtimeClient,
    private val tokenStore: TokenDataStore,
    private val dispatchers: DispatcherProvider
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val typingPerConversation = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        scope.launch {
            realtime.events.collect { ev ->
                when (ev) {
                    is RealtimeEvent.NewMessage -> messageDao.upsert(ev.message.toEntity())
                    is RealtimeEvent.StatusUpdate -> messageDao.updateStatus(
                        localId = ev.messageId,
                        status = ev.status.uppercase()
                    )
                    is RealtimeEvent.TypingChange -> typingPerConversation.update {
                        it + (ev.conversationId to ev.typing)
                    }
                    is RealtimeEvent.CampaignProgress -> Unit  // handled in CampaignRepository
                    RealtimeEvent.Connected, RealtimeEvent.Disconnected -> Unit
                }
            }
        }
    }

    override fun pagedMessages(conversationId: String): Flow<PagingData<Message>> =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 20,
                enablePlaceholders = false
            ),
            remoteMediator = MessageRemoteMediator(conversationId, chatApi, db),
            pagingSourceFactory = { messageDao.pagingSource(conversationId) }
        ).flow.map { pd -> pd.map { it.toDomain() } }

    override suspend fun sendText(
        conversationId: String,
        text: String,
        replyTo: String?
    ): String {
        val clientId = "local-" + UUID.randomUUID().toString()
        val now = Instant.now().toEpochMilli()
        // Stamp the active tenant so the locally-composed row is scoped like
        // synced rows; the cache is also wiped on logout/tenant switch.
        val tenantId = tokenStore.tenantId.first().orEmpty()
        val entity = MessageEntity(
            localId = clientId,
            tenantId = tenantId,
            serverId = null,
            clientId = clientId,
            conversationId = conversationId,
            text = text,
            sentAtEpochMs = now,
            outbound = true,
            status = "PENDING",
            type = "TEXT",
            mediaUrl = null,
            mediaMimeType = null,
            mediaSizeBytes = null,
            replyToMessageId = replyTo,
            errorReason = null
        )
        messageDao.upsert(entity)
        scope.launch { performSend(clientId) }
        return clientId
    }

    private suspend fun performSend(clientId: String): ApiResult<Unit> {
        val pending = messageDao.getByClientId(clientId)
            ?: return ApiResult.Error(message = "Message no longer exists")
        // conversationId IS the contactId. Backend has no reply-to support.
        val req = SendMessageRequest(
            contactId = pending.conversationId,
            text = pending.text,
            clientId = clientId
        )
        return when (val res = safeApiCall(dispatchers.io) { chatApi.send(req) }) {
            is ApiResult.Success -> {
                val dto = res.data.message
                messageDao.confirmServerId(
                    clientId = clientId,
                    serverId = dto.id,
                    status = dto.status.uppercase()
                )
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                Timber.w("Send failed: ${res.message}")
                if (res.type == ErrorType.Network) {
                    // Left PENDING, not FAILED: SendPendingWorker (enqueued by
                    // PendingMessageScheduler whenever connectivity returns) walks every
                    // PENDING row and retries it, so this goes out on its own once the
                    // device is back online instead of needing a manual retry tap.
                    Timber.i("Send deferred (offline), will retry on reconnect: $clientId")
                } else {
                    messageDao.setFailed(clientId, "FAILED", res.message)
                }
                res
            }
        }
    }

    override suspend fun retry(clientId: String): ApiResult<Unit> {
        messageDao.updateStatus(clientId, "PENDING")
        return performSend(clientId)
    }

    override suspend fun markRead(conversationId: String): ApiResult<Unit> {
        conversationDao.clearUnread(conversationId)
        return safeApiCall(dispatchers.io) { inboxApi.markRead(conversationId); Unit }
    }

    override suspend fun sendTyping(conversationId: String) {
        realtime.emitTyping(conversationId, true)
    }

    override fun observeTyping(conversationId: String): Flow<Boolean> =
        typingPerConversation.asStateFlow()
            .map { it[conversationId] ?: false }

    override suspend fun connectRealtime() = realtime.connect()
    override suspend fun disconnectRealtime() = realtime.disconnect()
}
