package com.ardym.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "campaigns",
    indices = [Index("createdAtEpochMs"), Index("status")]
)
data class CampaignEntity(
    @PrimaryKey val id: String,
    val name: String,
    val templateId: String,
    val templateName: String,
    val audienceTagsCsv: String,
    val audienceSize: Int,
    val status: String,
    val scheduledAtEpochMs: Long?,
    val sentCount: Long,
    val deliveredCount: Long,
    val readCount: Long,
    val failedCount: Long,
    val createdAtEpochMs: Long
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val language: String,
    val category: String,
    val status: String,
    val body: String,
    val variableCount: Int,
    val updatedAtEpochMs: Long
)
