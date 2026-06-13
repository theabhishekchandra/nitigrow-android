package com.websbaba.nitigrow.domain.model

import java.time.Instant

data class DashboardStats(
    val messagesSent: Long,
    val messagesDelivered: Long,
    val messagesRead: Long,
    val leadsTotal: Long,
    val leadsNew: Long,
    val activeCampaigns: Int,
    val revenueInr: Long,
    val deliveryRate: Float,    // 0.0..1.0
    val readRate: Float,        // 0.0..1.0
    val updatedAt: Instant
)
