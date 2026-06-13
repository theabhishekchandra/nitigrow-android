package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index("name"), Index("phone"), Index("nameKey")]
)
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameKey: String,        // lowercased + trimmed; for sort + search
    val phone: String,
    val email: String?,
    val avatarUrl: String?,
    val tagsCsv: String,        // pipe-joined
    val notes: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val isBlocked: Boolean
)
