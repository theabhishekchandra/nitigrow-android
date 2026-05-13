package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface InboxRepository {
    /** Cached stream. Pinned first, then by last message desc. */
    fun observeConversations(query: String = ""): Flow<List<Conversation>>
    suspend fun refresh(): ApiResult<Unit>
    suspend fun markRead(conversationId: String): ApiResult<Unit>
    suspend fun togglePin(conversationId: String, pinned: Boolean): ApiResult<Unit>
}
