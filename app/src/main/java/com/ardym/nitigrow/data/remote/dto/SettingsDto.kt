package com.ardym.nitigrow.data.remote.dto

// Mirrors GET /api/settings and the profile / auto-reply write endpoints.

data class SettingsDto(
    val businessName: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsapp: WhatsappDto = WhatsappDto(),
    val settings: TenantSettingsDto = TenantSettingsDto()
)

data class WhatsappDto(
    val connected: Boolean = false,
    val displayPhoneNumber: String? = null,
    val qualityRating: String = "UNKNOWN",
    val messagingTier: String = "TIER_1K",
    val dailyLimit: Int = 250,
    val dailyMsgCount: Int = 0
)

data class TenantSettingsDto(
    val autoReplies: AutoRepliesDto = AutoRepliesDto()
)

data class AutoRepliesDto(
    val welcome: AutoReplyEntryDto = AutoReplyEntryDto(),
    val away: AutoReplyEntryDto = AutoReplyEntryDto(),
    val outOfHours: AutoReplyEntryDto = AutoReplyEntryDto()
)

data class AutoReplyEntryDto(
    val enabled: Boolean = false,
    val message: String = ""
)

data class UpdateBusinessProfileRequest(
    val businessName: String? = null,
    val phone: String? = null
)

data class AutoReplyEntryRequest(
    val enabled: Boolean,
    val message: String
)

data class UpdateAutoRepliesRequest(
    val welcome: AutoReplyEntryRequest? = null,
    val away: AutoReplyEntryRequest? = null,
    val outOfHours: AutoReplyEntryRequest? = null
)
