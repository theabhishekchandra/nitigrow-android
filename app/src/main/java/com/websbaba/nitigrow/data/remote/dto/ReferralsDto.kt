package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

// Mirrors the backend /referrals and /loyalty JSON (Phase 10 H1–H3).

data class ReferralProgramDto(
    val enabled: Boolean = false,
    val rewardType: String = "loyalty_points",
    val referrerReward: Int = 0,
    val refereeReward: Int = 0,
    val qualifyOn: String = "first_purchase",
    val shareMessage: String? = null
)

data class UpdateReferralProgramRequest(
    val enabled: Boolean
)

data class ReferralStatsDto(
    val pending: Int = 0,
    val qualified: Int = 0,
    val rewarded: Int = 0,
    val expired: Int = 0,
    val total: Int = 0
)

data class ReferralLeaderDto(
    val contactId: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val rewarded: Int = 0
)

data class SaasReferralDto(
    val code: String = "",
    val signupLink: String = "",
    val creditPaise: Long = 0,
    val counts: SaasReferralCountsDto = SaasReferralCountsDto(),
    val creditPerReferralPaise: Long = 0
)

data class SaasReferralCountsDto(
    @SerializedName("signed_up") val signedUp: Int = 0,
    val converted: Int = 0,
    val credited: Int = 0
)

data class LoyaltyProgramDto(
    val enabled: Boolean = false,
    val pointsPerRupee: Double = 0.0,
    val paisePerPoint: Int = 0,
    val minRedeemPoints: Int = 0
)
