package com.ardym.nitigrow.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.realtime.RealtimeClient
import com.ardym.nitigrow.core.realtime.RealtimeEvent
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.local.NitiGrowDatabase
import com.ardym.nitigrow.data.local.dao.ConversationDao
import com.ardym.nitigrow.data.local.dao.MessageDao
import com.ardym.nitigrow.data.local.entity.MessageEntity
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.ChatApi
import com.ardym.nitigrow.data.remote.api.InboxApi
import com.ardym.nitigrow.data.remote.dto.SendMessageRequest
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
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
        val entity = MessageEntity(
            localId = clientId,
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

    private suspend fun performSend(clientId: String) {
        val pending = messageDao.getByClientId(clientId) ?: return
        // conversationId IS the contactId. Backend has no reply-to support.
        val req = SendMessageRequest(
            contactId = pending.conversationId,
            text = pending.text,
            clientId = clientId
        )
        when (val res = safeApiCall(dispatchers.io) { chatApi.send(req) }) {
            is ApiResult.Success -> {
                val dto = res.data.message
                messageDao.confirmServerId(
                    clientId = clientId,
                    serverId = dto.id,
                    status = dto.status.uppercase()
                )
            }
            is ApiResult.Error -> {
                Timber.w("Send failed: ${res.message}")
                messageDao.setFailed(clientId, "FAILED", res.message)
            }
        }
    }

    override suspend fun retry(clientId: String): ApiResult<Unit> {
        messageDao.updateStatus(clientId, "PENDING")
        performSend(clientId)
        return when (messageDao.getByClientId(clientId)?.status) {
            "FAILED" -> ApiResult.Error(message = "Retry failed")
            else -> ApiResult.Success(Unit)
        }
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
            .filter { true }

    override suspend fun connectRealtime() = realtime.connect()
    override suspend fun disconnectRealtime() = realtime.disconnect()
}
