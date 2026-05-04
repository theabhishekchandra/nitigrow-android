package com.ardym.nitigrow.domain.model

import java.time.Instant

data class Lead(
    val id: String,
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val source: String,
    val stage: LeadStage,
    val valueInr: Long,
    val ownerName: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class LeadStage(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    PROPOSAL("Proposal"),
    WON("Won"),
    LOST("Lost");

    companion object {
        fun safeValueOf(raw: String): LeadStage =
            runCatching { valueOf(raw.uppercase()) }.getOrDefault(NEW)
    }
}
