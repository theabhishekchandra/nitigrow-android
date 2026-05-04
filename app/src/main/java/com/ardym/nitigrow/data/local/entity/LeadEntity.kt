package com.ardym.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leads",
    indices = [Index("stage"), Index("updatedAtEpochMs")]
)
data class LeadEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val source: String,
    val stage: String,
    val valueInr: Long,
    val ownerName: String?,
    val notes: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
