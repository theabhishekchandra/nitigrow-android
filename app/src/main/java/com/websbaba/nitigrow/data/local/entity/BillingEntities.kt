package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Singleton row (id = 0) caching the latest GET /billing/status snapshot.
@Entity(tableName = "billing_status")
data class BillingStatusEntity(
    @PrimaryKey val id: Int = 0,
    val plan: String,
    val accountStatus: String?,
    val subStatus: String?,
    val trialEndsAtEpochMs: Long?,
    val periodStartEpochMs: Long?,
    val periodEndEpochMs: Long?,
    val gatewaySubscriptionId: String?,
    val cancelAtPeriodEnd: Boolean,
    val billingCycle: String?,
    val msgUsed: Int,
    val msgLimit: Int,
    val aiUsed: Int,
    val aiLimit: Int,
    val contactsUsed: Int,
    val contactsLimit: Int,
    val usersUsed: Int,
    val usersLimit: Int,
    val pricesJson: String,
    val referralCreditPaise: Int,
    val branding: Boolean,
)

@Entity(
    tableName = "invoices",
    indices = [Index("paidAtEpochMs")]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val number: String?,
    val status: String?,
    val amountPaise: Long,
    val paidAtEpochMs: Long?,
)
