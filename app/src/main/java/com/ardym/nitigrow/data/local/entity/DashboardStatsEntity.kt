package com.ardym.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table. id always = 0. Latest snapshot for current tenant.
 * Tenant switch must clear table.
 */
@Entity(tableName = "dashboard_stats")
data class DashboardStatsEntity(
    @PrimaryKey val id: Int = 0,
    val messagesSent: Long,
    val messagesDelivered: Long,
    val messagesRead: Long,
    val leadsTotal: Long,
    val leadsNew: Long,
    val activeCampaigns: Int,
    val revenueInr: Long,
    val updatedAtEpochMs: Long
)
