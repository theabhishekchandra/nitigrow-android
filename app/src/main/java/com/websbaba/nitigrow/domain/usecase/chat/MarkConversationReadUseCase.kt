package com.websbaba.nitigrow.domain.usecase.chat

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ChatRepository
import javax.inject.Inject

class MarkConversationReadUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(conversationId: String): ApiResult<Unit> =
        repo.markRead(conversationId)
}
