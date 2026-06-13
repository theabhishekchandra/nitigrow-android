package com.websbaba.nitigrow.presentation.feature.analytics

import java.time.LocalDate

/** Date-range choices. The header pills map 7D/30D/90D onto WEEK/MONTH/QUARTER. */
enum class DateRange { TODAY, WEEK, MONTH, QUARTER, CUSTOM }

/** One bar in the daily-sent line chart. */
data class DailyPoint(
    val day: LocalDate,
    val sent: Int,
    val delivered: Int,
    val read: Int,
)

/** Aggregated template throughput row in the "Top templates" list. */
data class TemplatePerf(
    val name: String,
    val sent: Int,
    /** 0f..1f read-rate (delivered → read conversion). */
    val readRate: Float,
)

/** Per-agent productivity row. */
data class AgentPerf(
    val name: String,
    val chats: Int,
    val avgResponseSeconds: Int,
)

data class AnalyticsUiState(
    val range: DateRange = DateRange.WEEK,
    val timeSeries: List<DailyPoint> = emptyList(),
    val templatePerformance: List<TemplatePerf> = emptyList(),
    val agentPerformance: List<AgentPerf> = emptyList(),
    val isLoading: Boolean = false,
) {
    /** Total messages sent across the time series — derived for the Sent stat card. */
    val totalSent: Int get() = timeSeries.sumOf { it.sent }

    /** Total messages delivered across the time series. */
    val totalDelivered: Int get() = timeSeries.sumOf { it.delivered }

    /** Total messages read across the time series. */
    val totalRead: Int get() = timeSeries.sumOf { it.read }

    /** Delivery rate (delivered / sent). Returns 0f when there are no messages. */
    val deliveryRate: Float
        get() = if (totalSent == 0) 0f else totalDelivered.toFloat() / totalSent

    /** Read rate (read / delivered). Returns 0f when there are no deliveries. */
    val readRate: Float
        get() = if (totalDelivered == 0) 0f else totalRead.toFloat() / totalDelivered
}
