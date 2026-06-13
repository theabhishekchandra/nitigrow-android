package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val priceInr: Long,
    val periodDays: Int,
    val featuresCsv: String,
    val isPopular: Boolean
)

@Entity(tableName = "subscription")
data class SubscriptionEntity(
    @PrimaryKey val id: Int = 0,    // singleton row
    val planId: String,
    val planName: String,
    val status: String,
    val renewsAtEpochMs: Long?,
    val cancelledAtEpochMs: Long?
)

@Entity(
    tableName = "payments",
    indices = [Index("createdAtEpochMs")]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val amountInr: Long,
    val status: String,
    val method: String?,
    val planName: String?,
    val createdAtEpochMs: Long
)
