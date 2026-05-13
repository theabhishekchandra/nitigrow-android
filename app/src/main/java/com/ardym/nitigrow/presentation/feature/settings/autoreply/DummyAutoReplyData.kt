package com.ardym.nitigrow.presentation.feature.settings.autoreply

import java.time.LocalTime

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Seeds [AutoReplyViewModel] until `/api/tenant/auto-reply` is wired by the parent.
object DummyAutoReplyData {

    private val AWAY_START: LocalTime = LocalTime.of(18, 0)
    private val AWAY_END: LocalTime = LocalTime.of(9, 0)

    private const val WELCOME_MESSAGE =
        "Namaste! Thanks for reaching out to ARDYM. " +
            "We typically reply within 30 minutes during business hours (9am–6pm IST)."

    private const val AWAY_MESSAGE =
        "Currently outside business hours (9am–6pm IST). " +
            "We'll get back to you first thing in the morning — thank you for your patience!"

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun config(): AutoReplyUiState = AutoReplyUiState(
        welcomeEnabled = true,
        welcomeMessage = WELCOME_MESSAGE,
        awayEnabled = true,
        awayMessage = AWAY_MESSAGE,
        awayStart = AWAY_START,
        awayEnd = AWAY_END
    )
}
