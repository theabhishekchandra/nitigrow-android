package com.websbaba.nitigrow.domain.model

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

        /**
         * Reads a stage from the backend, whose vocabulary is `new / warm / hot / won / lost`.
         * `warm` and `hot` have no exact twin here, so for display they stand in for the two
         * mid-pipeline stages (Contacted, Qualified). Anything unrecognised reads as New.
         */
        fun fromBackend(raw: String?): LeadStage = when (raw?.trim()?.lowercase()) {
            "warm" -> CONTACTED
            "hot" -> QUALIFIED
            null, "" -> NEW
            else -> safeValueOf(raw)
        }
    }

    /**
     * This stage as the backend's five-value vocabulary (`new/warm/hot/won/lost` —
     * see backend/src/models/Lead.js). The backend has no slot for PROPOSAL, the
     * last stage before a close; it folds into "hot" alongside QUALIFIED, same as
     * [fromBackend] reads "hot" back as QUALIFIED. A PATCH that instead sent the
     * enum's own name (e.g. "QUALIFIED") was rejected 400 by the backend's strict
     * enum check, so every stage move was previously failing silently.
     */
    fun toBackend(): String = when (this) {
        NEW -> "new"
        CONTACTED -> "warm"
        QUALIFIED, PROPOSAL -> "hot"
        WON -> "won"
        LOST -> "lost"
    }
}
