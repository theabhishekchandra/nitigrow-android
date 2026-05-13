package com.ardym.nitigrow.presentation.feature.settings.autoreply

/**
 * UI state for the Auto-reply settings sub-screen.
 *
 * Combines the welcome-message and out-of-hours configs into a single state
 * so the screen can render both cards from one StateFlow.
 */
data class AutoReplyUiState(
    val welcomeEnabled: Boolean = true,
    val welcomeMessage: String = "",
    val awayEnabled: Boolean = false,
    val awayMessage: String = "",
    val awayStart: java.time.LocalTime = java.time.LocalTime.of(18, 0),
    val awayEnd: java.time.LocalTime = java.time.LocalTime.of(9, 0),
    val isSaving: Boolean = false
)

sealed interface AutoReplyEffect {
    data class Toast(val text: String) : AutoReplyEffect
}
