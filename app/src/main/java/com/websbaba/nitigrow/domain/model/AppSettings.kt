package com.websbaba.nitigrow.domain.model

/** Flattened tenant settings consumed by the settings sub-screens. */
data class AppSettings(
    val businessName: String,
    val email: String,
    val phone: String,
    val displayPhoneNumber: String?,
    val whatsappConnected: Boolean,
    val qualityRating: String,
    val messagingTier: String,
    val dailyLimit: Int,
    val dailyMsgCount: Int,
    val welcomeEnabled: Boolean,
    val welcomeMessage: String,
    val awayEnabled: Boolean,
    val awayMessage: String
)
