package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.local.dao.ConversationDao
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.InboxApi
import com.ardym.nitigrow.data.remote.dto.TogglePinRequest
import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepositoryImpl @Inject constructor(
    private val api: InboxApi,
    private val dao: ConversationDao,
    private val dispatchers: DispatcherProvider
) : InboxRepository {

    override fun observeConversations(query: String): Flow<List<Conversation>> {
        val source = if (query.isBlank()) dao.observeAll() else dao.search(query)
        return source.map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun refresh(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> {
                dao.upsertAll(res.data.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun markRead(conversationId: String): ApiResult<Unit> {
        // optimistic local update
        dao.clearUnread(conversationId)
        return safeApiCall(dispatchers.io) { api.markRead(conversationId); Unit }
    }

    override suspend fun togglePin(conversationId: String, pinned: Boolean): ApiResult<Unit> {
        dao.setPinned(conversationId, pinned)
        return safeApiCall(dispatchers.io) {
            api.togglePin(conversationId, TogglePinRequest(pinned)); Unit
        }
    }
}
