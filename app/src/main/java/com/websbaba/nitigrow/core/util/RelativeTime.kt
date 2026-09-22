package com.websbaba.nitigrow.core.util

import java.time.Instant
import java.time.temporal.ChronoUnit

/** "2h ago" / "3d ago" / "just now" — the same shorthand the web app uses. Future instants read as "just now". */
fun relativeTime(then: Instant, now: Instant = Instant.now()): String {
    val seconds = ChronoUnit.SECONDS.between(then, now).coerceAtLeast(0)
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
