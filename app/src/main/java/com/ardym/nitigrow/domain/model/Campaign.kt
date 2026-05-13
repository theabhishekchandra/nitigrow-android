package com.ardym.nitigrow.domain.model

import java.time.Instant

data class Campaign(
    val id: String,
    val name: String,
    val templateId: String,
    val templateName: String,
    val audienceTags: List<String>,
    val audienceSize: Int,
    val status: CampaignStatus,
    val scheduledAt: Instant?,
    val sentCount: Long,
    val deliveredCount: Long,
    val readCount: Long,
    val failedCount: Long,
    val createdAt: Instant
) {
    val deliveryRate: Float
        get() = if (sentCount > 0) deliveredCount.toFloat() / sentCount else 0f
    val readRate: Float
        get() = if (deliveredCount > 0) readCount.toFloat() / deliveredCount else 0f
}

enum class CampaignStatus {
    DRAFT, SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED;

    companion object {
        fun safeValueOf(raw: String): CampaignStatus =
            runCatching { valueOf(raw.uppercase()) }.getOrDefault(DRAFT)
    }
}

data class Template(
    val id: String,
    val name: String,
    val language: String,
    val category: String,           // MARKETING / UTILITY / AUTHENTICATION
    val status: String,             // APPROVED / PENDING / REJECTED
    val body: String,
    val variableCount: Int,
    val updatedAt: Instant
)
