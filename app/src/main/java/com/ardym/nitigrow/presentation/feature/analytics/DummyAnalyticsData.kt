// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
package com.ardym.nitigrow.presentation.feature.analytics

import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.random.Random

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Produces a deterministic-but-realistic analytics snapshot for the supplied date range.
// Daily sent counts oscillate around an SMB-style baseline (~500-2000/day) using a
// per-range Random seed so the chart looks lively while staying stable across recompositions.
object DummyAnalyticsData {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun snapshot(range: DateRange): AnalyticsUiState {
        val days = when (range) {
            DateRange.TODAY -> 1
            DateRange.WEEK -> 7
            DateRange.MONTH -> 30
            DateRange.CUSTOM -> 14
        }
        return AnalyticsUiState(
            range = range,
            timeSeries = generateTimeSeries(days, seed = days.toLong()),
            templatePerformance = topTemplates(),
            agentPerformance = agents(),
            isLoading = false,
        )
    }

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private fun generateTimeSeries(days: Int, seed: Long): List<DailyPoint> {
        val rnd = Random(seed)
        val today = LocalDate.now()
        return (0 until days).map { offset ->
            val day = today.minusDays((days - 1 - offset).toLong())
            // Baseline 800/day, ± ~600 jitter, weekend dip ~20%.
            val weekendDip = if (day.dayOfWeek.value >= 6) 0.8f else 1.0f
            val sent = ((800 + rnd.nextInt(0, 1200)) * weekendDip).roundToInt()
            val delivered = (sent * (0.93f + rnd.nextFloat() * 0.04f)).roundToInt() // ~93-97%
            val read = (delivered * (0.65f + rnd.nextFloat() * 0.10f)).roundToInt() // ~65-75% of delivered
            DailyPoint(day = day, sent = sent, delivered = delivered, read = read)
        }
    }

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private fun topTemplates(): List<TemplatePerf> = listOf(
        TemplatePerf(name = "order_confirmation", sent = 4_820, readRate = 0.82f),
        TemplatePerf(name = "payment_received", sent = 3_410, readRate = 0.78f),
        TemplatePerf(name = "diwali_offer_2026", sent = 6_120, readRate = 0.71f),
        TemplatePerf(name = "appointment_reminder", sent = 1_840, readRate = 0.66f),
        TemplatePerf(name = "cart_abandoned", sent = 2_310, readRate = 0.43f),
    )

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private fun agents(): List<AgentPerf> = listOf(
        AgentPerf(name = "Pankaj", chats = 184, avgResponseSeconds = 92),
        AgentPerf(name = "Sneha", chats = 211, avgResponseSeconds = 68),
        AgentPerf(name = "Karan", chats = 146, avgResponseSeconds = 124),
        AgentPerf(name = "Riya", chats = 178, avgResponseSeconds = 105),
        AgentPerf(name = "Vivek", chats = 132, avgResponseSeconds = 138),
        AgentPerf(name = "Priya", chats = 165, avgResponseSeconds = 88),
        AgentPerf(name = "Amit", chats = 119, avgResponseSeconds = 152),
        AgentPerf(name = "Neha", chats = 198, avgResponseSeconds = 77),
    )
}
