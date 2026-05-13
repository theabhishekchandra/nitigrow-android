package com.ardym.nitigrow.domain.repository

import androidx.paging.PagingData
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageType
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun pagedMessages(conversationId: String): Flow<PagingData<Message>>

    /** Optimistic local insert returns clientId; backend call runs in background. */
    suspend fun sendText(conversationId: String, text: String, replyTo: String? = null): String

    suspend fun retry(clientId: String): ApiResult<Unit>

    suspend fun markRead(conversationId: String): ApiResult<Unit>

    /** Local outgoing typing event over WS. */
    suspend fun sendTyping(conversationId: String)

    /** Inbound typing indicator from peer. */
    fun observeTyping(conversationId: String): Flow<Boolean>

    suspend fun connectRealtime()
    suspend fun disconnectRealtime()
}
