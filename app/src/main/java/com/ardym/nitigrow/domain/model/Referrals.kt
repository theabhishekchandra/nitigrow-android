package com.ardym.nitigrow.domain.model

/** Customer-gets-customer referral program config (Phase 10 H1). */
data class ReferralProgram(
    val enabled: Boolean,
    val rewardType: String,
    val referrerReward: Int,
    val refereeReward: Int,
    val qualifyOn: String,
    val shareMessage: String
)

/** Lifetime referral funnel counts. */
data class ReferralFunnel(
    val pending: Int,
    val qualified: Int,
    val rewarded: Int,
    val expired: Int,
    val total: Int
)

/** A top referrer (rewarded count). */
data class ReferralLeader(
    val name: String?,
    val phone: String?,
    val rewarded: Int
)

/** SaaS "refer a business" status for this tenant (Phase 10 H2). */
data class SaasReferral(
    val code: String,
    val signupLink: String,
    val creditPaise: Long,
    val signedUp: Int,
    val converted: Int,
    val creditPerReferralPaise: Long
)

/** Loyalty program config (Phase 10 H3). */
data class LoyaltyProgram(
    val enabled: Boolean,
    val pointsPerRupee: Double,
    val paisePerPoint: Int,
    val minRedeemPoints: Int
)
