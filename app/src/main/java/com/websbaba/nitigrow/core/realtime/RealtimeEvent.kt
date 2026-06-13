package com.websbaba.nitigrow.core.realtime

import com.websbaba.nitigrow.data.remote.dto.MessageDto

sealed interface RealtimeEvent {
    data class NewMessage(val message: MessageDto) : RealtimeEvent
    data class StatusUpdate(
        val messageId: String,
        val status: String
    ) : RealtimeEvent
    data class TypingChange(
        val conversationId: String,
        val typing: Boolean
    ) : RealtimeEvent
    data class CampaignProgress(
        val campaignId: String,
        val status: String,
        val sent: Long,
        val delivered: Long,
        val read: Long,
        val failed: Long
    ) : RealtimeEvent
    data object Connected : RealtimeEvent
    data object Disconnected : RealtimeEvent
}
