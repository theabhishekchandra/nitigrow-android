package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire model for `GET /api/analytics/overview`.
 *
 * The backend responds with:
 * ```
 * {
 *   "stats": {
 *     "totalContacts": Int,
 *     "contactsDelta": Double,        // % change vs prior 30d window
 *     "openConversations": Int,
 *     "messagesSent30d": Int,
 *     "messagesDelta": Double,        // % change vs prior 30d window
 *     "responseRate": Int,            // inbound/outbound %, 0..100+
 *     "resolved30d": Int
 *   },
 *   "recentCampaigns": [ { name, stats, sentAt }, ... ]
 * }
 * ```
 *
 * The dashboard screen/mapper consume the legacy [DashboardStatsDto] shape, so the
 * raw overview is converted via [AnalyticsOverviewDto.toStats]. This keeps the API
 * pointed at the real route while leaving downstream (mapper/entity/screen) untouched.
 */
data class AnalyticsOverviewDto(
    @SerializedName("stats") val stats: OverviewStatsDto = OverviewStatsDto(),
    @SerializedName("recentCampaigns") val recentCampaigns: List<OverviewCampaignDto> = emptyList()
) {
    fun toStats(): DashboardStatsDto {
        // The backend exposes outbound delivery as a single response rate (inbound/outbound)
        // rather than a delivered/read funnel. Until a dedicated funnel endpoint exists we
        // surface responseRate as the "read" proxy and treat every sent message as delivered,
        // so the existing delivery/read cards still render meaningful numbers.
        val sent = stats.messagesSent30d.toLong()
        val responsePct = stats.responseRate.coerceIn(0, 100)
        val responded = (sent * responsePct) / 100L
        return DashboardStatsDto(
            messagesSent = sent,
            messagesDelivered = sent,
            messagesRead = responded,
            leadsTotal = stats.totalContacts.toLong(),
            leadsNew = newContactsEstimate(),
            activeCampaigns = recentCampaigns.size,
            revenueInr = 0L
        )
    }

    /**
     * Backend returns a percentage delta (contactsDelta) rather than an absolute new-contact
     * count. Reconstruct the approximate new contacts in the last 30d from the delta so the
     * "+N new" subtitle stays informative.
     */
    private fun newContactsEstimate(): Long {
        val total = stats.totalContacts.toLong()
        val delta = stats.contactsDelta
        if (total <= 0L || delta <= 0.0) return 0L
        // delta = (new30 - new60) / new60 * 100  → not directly invertible to new30 without
        // new60; approximate "new" as the share of growth implied by the delta on the total.
        val approx = (total * delta / (100.0 + delta)).toLong()
        return approx.coerceIn(0L, total)
    }
}

data class OverviewStatsDto(
    @SerializedName("totalContacts") val totalContacts: Int = 0,
    @SerializedName("contactsDelta") val contactsDelta: Double = 0.0,
    @SerializedName("openConversations") val openConversations: Int = 0,
    @SerializedName("messagesSent30d") val messagesSent30d: Int = 0,
    @SerializedName("messagesDelta") val messagesDelta: Double = 0.0,
    @SerializedName("responseRate") val responseRate: Int = 0,
    @SerializedName("resolved30d") val resolved30d: Int = 0
)

data class OverviewCampaignDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sentAt") val sentAt: String? = null
)

/**
 * Legacy stats shape consumed by [com.websbaba.nitigrow.data.mapper] and the dashboard screen.
 * Populated from [AnalyticsOverviewDto.toStats]; field names/types are preserved so the
 * mapper, Room entity, and Compose screen compile unchanged.
 */
data class DashboardStatsDto(
    @SerializedName("messagesSent") val messagesSent: Long,
    @SerializedName("messagesDelivered") val messagesDelivered: Long,
    @SerializedName("messagesRead") val messagesRead: Long,
    @SerializedName("leadsTotal") val leadsTotal: Long,
    @SerializedName("leadsNew") val leadsNew: Long,
    @SerializedName("activeCampaigns") val activeCampaigns: Int,
    @SerializedName("revenueInr") val revenueInr: Long
)
