package com.websbaba.nitigrow.domain.usecase.chat

import com.websbaba.nitigrow.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, text: String): String {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Message cannot be empty" }
        return repo.sendText(conversationId, trimmed)
    }
}
