package com.websbaba.nitigrow.presentation.feature.leads.components

import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// INR formatting helpers shared by the leads kanban / list / detail screens.
// ─────────────────────────────────────────────────────────────────────────────

private val inrFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

/** Indian digit grouping, e.g. 140000 → "₹1,40,000". */
internal fun formatInr(amount: Long): String = "₹" + inrFormat.format(amount)

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

/** "2h ago" / "3d ago" / "just now" — the same shorthand the web app uses. */
internal fun relativeTime(then: java.time.Instant, now: java.time.Instant = java.time.Instant.now()): String {
    val seconds = java.time.temporal.ChronoUnit.SECONDS.between(then, now).coerceAtLeast(0)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 45 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> "${days / 30}mo ago"
    }
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
