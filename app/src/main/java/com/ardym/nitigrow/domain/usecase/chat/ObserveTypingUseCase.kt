package com.ardym.nitigrow.domain.usecase.chat

import com.ardym.nitigrow.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTypingUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<Boolean> =
        repo.observeTyping(conversationId)
}
