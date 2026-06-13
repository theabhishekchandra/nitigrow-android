package com.websbaba.nitigrow.presentation.feature.conflicts

import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// ConflictUiState — state model for the offline-sync conflict resolution screen.
// Spec: phase-3-mobile.md Section 4.2 "Conflict Resolution Rules".
//
// Each [SyncConflict] represents a single divergence the device detected when
// it came back online. The user can resolve it manually (use local / use
// server / dismiss) — otherwise it'll be auto-resolved by the [ConflictRule].
// ─────────────────────────────────────────────────────────────────────────────

data class ConflictUiState(
    val conflicts: List<SyncConflict> = emptyList(),
    val isResolving: Boolean = false,
)

data class SyncConflict(
    val id: String,
    val entityType: String,    // "contact" | "message" | "campaign"
    val entityName: String,    // e.g., "Priya Sharma" or "Diwali Offer 2026"
    val detectedAt: Instant,
    val description: String,   // human-readable, e.g., "Edited offline + on web at the same time"
    val localValue: String,
    val serverValue: String,
    val rule: ConflictRule,    // how it'd be auto-resolved if user does nothing
)

/**
 * Auto-resolution rule lifted from phase-3 Section 4.2.
 * • SERVER_WINS — server-timestamp wins (default for edits)
 * • LOCAL_WINS  — local pending change should win (rare; e.g., offline draft
 *   that the server has no view of yet)
 * • ASK_USER    — no safe default; force the agent to pick (e.g., deletion +
 *   pending edit)
 */
enum class ConflictRule {
    SERVER_WINS,
    LOCAL_WINS,
    ASK_USER,
}

sealed interface ConflictAction {
    data object UseServer : ConflictAction
    data object UseLocal : ConflictAction
    data object Dismiss : ConflictAction
}
