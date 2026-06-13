package com.websbaba.nitigrow.domain.model

/** One day of message volume from /api/analytics/messages-per-day. */
data class DailyMetric(
    val date: String,   // YYYY-MM-DD
    val sent: Int,
    val delivered: Int,
    val read: Int
)

/** An agent's reply count from /api/analytics/top-agents. */
data class AgentMetric(
    val name: String,
    val replies: Int
)
