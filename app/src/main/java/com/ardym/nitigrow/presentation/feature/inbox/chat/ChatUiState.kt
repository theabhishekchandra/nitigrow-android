package com.ardym.nitigrow.presentation.feature.inbox.chat

data class ChatUiState(
    val conversationId: String,
    val draft: String = "",
    val typing: Boolean = false,
    val error: String? = null
)

sealed interface ChatEffect {
    data class ShowError(val message: String) : ChatEffect
}
