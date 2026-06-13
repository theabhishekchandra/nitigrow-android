package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.ConversationDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.InboxApi
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.repository.InboxRepository
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
        // GET messages/conversations returns a bare array (no envelope).
        when (val res = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> {
                dao.upsertAll(res.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun markRead(conversationId: String): ApiResult<Unit> {
        // optimistic local update; conversationId IS the contactId.
        dao.clearUnread(conversationId)
        return safeApiCall(dispatchers.io) { api.markRead(conversationId); Unit }
    }

    override suspend fun togglePin(conversationId: String, pinned: Boolean): ApiResult<Unit> {
        // No backend route for pin — keep the optimistic local state only so the
        // UI behaves, but there is nothing to persist server-side.
        dao.setPinned(conversationId, pinned)
        return ApiResult.Success(Unit)
    }
}
