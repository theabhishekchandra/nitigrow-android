package com.ardym.nitigrow.presentation.feature.leads.components

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
