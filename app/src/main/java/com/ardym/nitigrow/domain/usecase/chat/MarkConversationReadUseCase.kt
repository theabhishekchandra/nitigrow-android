package com.ardym.nitigrow.domain.usecase.chat

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.ChatRepository
import javax.inject.Inject

class MarkConversationReadUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(conversationId: String): ApiResult<Unit> =
        repo.markRead(conversationId)
}
