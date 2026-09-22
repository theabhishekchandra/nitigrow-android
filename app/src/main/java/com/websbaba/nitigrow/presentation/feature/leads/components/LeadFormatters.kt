package com.websbaba.nitigrow.presentation.feature.leads.components

import com.websbaba.nitigrow.core.util.relativeTime
import com.websbaba.nitigrow.core.util.formatIndian
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// INR formatting helpers shared by the leads kanban / list / detail screens.
// ─────────────────────────────────────────────────────────────────────────────

/** Indian digit grouping, e.g. 140000 → "₹1,40,000". */
internal fun formatInr(amount: Long): String = "₹" + formatIndian(amount)

/**
 * Compact column-sum format matching the web design:
 * ≥ 1 lakh → "₹1.4L" (one decimal), otherwise Indian-grouped "₹95,000".
 */
internal fun formatInrCompact(amount: Long): String =
    if (amount >= 100_000L) {
        "₹" + String.format(Locale.ENGLISH, "%.1f", amount / 100_000.0) + "L"
    } else {
        formatInr(amount)
    }

/** "WhatsApp inbound · Anita · 2h ago" — source, owner (or Unassigned) and last update. */
internal fun leadMeta(
    source: String,
    ownerName: String?,
    updatedAt: java.time.Instant,
    now: java.time.Instant = java.time.Instant.now()
): String = listOf(
    source.takeIf { it.isNotBlank() },
    ownerName?.takeIf { it.isNotBlank() } ?: "Unassigned",
    relativeTime(updatedAt, now)
).filterNotNull().joinToString(" · ")
