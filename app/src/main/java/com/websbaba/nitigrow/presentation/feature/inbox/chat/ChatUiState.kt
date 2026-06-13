package com.websbaba.nitigrow.presentation.feature.inbox.chat

import java.time.Instant

data class ChatUiState(
    val conversationId: String,
    val draft: String = "",
    val typing: Boolean = false,
    val error: String? = null,
    // Header data, observed from the conversation list cache.
    val contactName: String = "",
    val contactPhone: String = "",
    val avatarUrl: String? = null,
    /**
     * When the 24h customer-service window closes. Derivable only while the
     * conversation's last message is inbound (window restarts on every inbound
     * message); null means unknown / not applicable and hides the header pill.
     */
    val windowExpiresAt: Instant? = null
)

sealed interface ChatEffect {
    data class ShowError(val message: String) : ChatEffect
}
