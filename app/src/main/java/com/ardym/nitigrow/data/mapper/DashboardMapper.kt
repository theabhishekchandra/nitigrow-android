package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.DashboardStatsEntity
import com.ardym.nitigrow.data.remote.dto.DashboardStatsDto
import com.ardym.nitigrow.domain.model.DashboardStats
import java.time.Instant

fun DashboardStatsDto.toEntity(): DashboardStatsEntity = DashboardStatsEntity(
    id = 0,
    messagesSent = messagesSent,
    messagesDelivered = messagesDelivered,
    messagesRead = messagesRead,
    leadsTotal = leadsTotal,
    leadsNew = leadsNew,
    activeCampaigns = activeCampaigns,
    revenueInr = revenueInr,
    updatedAtEpochMs = System.currentTimeMillis()
)

fun DashboardStatsEntity.toDomain(): DashboardStats {
    val deliveryRate = if (messagesSent > 0) messagesDelivered.toFloat() / messagesSent else 0f
    val readRate = if (messagesDelivered > 0) messagesRead.toFloat() / messagesDelivered else 0f
    return DashboardStats(
        messagesSent = messagesSent,
        messagesDelivered = messagesDelivered,
        messagesRead = messagesRead,
        leadsTotal = leadsTotal,
        leadsNew = leadsNew,
        activeCampaigns = activeCampaigns,
        revenueInr = revenueInr,
        deliveryRate = deliveryRate.coerceIn(0f, 1f),
        readRate = readRate.coerceIn(0f, 1f),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMs)
    )
}
