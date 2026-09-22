package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// WindowStatusBanner — full-width status of WhatsApp's 24h customer-service
// window, shown under the chat header. See docs/phase-3-mobile.md
// "WhatsApp 24-Hour Messaging Window".
//
//   window open   → "Reply window open · closes in 2h 14m"   (terracotta)
//   window closed → "Window closed. Send an approved template to reopen it."
//   unknown (null expiry — last message was outbound) → renders nothing
//
// The countdown re-evaluates once a minute; the ticker restarts whenever the
// expiry changes (i.e. a new inbound message reopens the window).
// ─────────────────────────────────────────────────────────────────────────────

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE
private const val TICKER_MILLIS = 60_000L

/**
 * "2h 14m", "45m" or "1h" for the time left in an open window. Sub-minute
 * remainders round up to "1m" so an open window never reads "0m".
 */
internal fun formatWindowRemaining(remainingMillis: Long): String {
    val hours = remainingMillis / MILLIS_PER_HOUR
    val minutes = (remainingMillis % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes.coerceAtLeast(1L)}m"
    }
}

/**
 * Whether the 24h customer-service window is open right now, re-evaluated once a minute.
 * A null [windowExpiresAt] (no messages yet, or the last one was outbound) reads as open —
 * there's nothing confirming it's closed, so the composer isn't blocked on a guess.
 */
@Composable
fun rememberWindowOpen(windowExpiresAt: Instant?): Boolean {
    if (windowExpiresAt == null) return true
    var nowMillis by remember(windowExpiresAt) {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(windowExpiresAt) {
        while (true) {
            delay(TICKER_MILLIS)
            nowMillis = System.currentTimeMillis()
        }
    }
    return windowExpiresAt.toEpochMilli() - nowMillis > 0L
}

@Composable
fun WindowStatusBanner(windowExpiresAt: Instant?, modifier: Modifier = Modifier) {
    if (windowExpiresAt == null) return

    // Own ticker (rather than rememberWindowOpen) so the "closes in Xh Ym" label keeps
    // counting down live, not just the open/closed boolean.
    var nowMillis by remember(windowExpiresAt) {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(windowExpiresAt) {
        while (true) {
            delay(TICKER_MILLIS)
            nowMillis = System.currentTimeMillis()
        }
    }
    val remainingMillis = windowExpiresAt.toEpochMilli() - nowMillis
    val open = remainingMillis > 0L
    val tone = if (open) Niti.colors.tertiaryTone else Niti.colors.errorTone
    val label = if (open) {
        "Reply window open · closes in ${formatWindowRemaining(remainingMillis)}"
    } else {
        "Window closed. Send an approved template to reopen it."
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tone.container)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = if (open) NitiIcons.Clock else NitiIcons.Lock,
            contentDescription = null,
            tint = tone.onContainer,
            modifier = Modifier.size(18.dp)
        )
        Text(text = label, style = NitiType.label, color = tone.onContainer)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Window — open", showBackground = true)
@Composable
private fun PreviewWindowOpen() {
    NitiGrowTheme {
        WindowStatusBanner(windowExpiresAt = Instant.now().plus(134, ChronoUnit.MINUTES))
    }
}

@Preview(name = "Window — closed", showBackground = true)
@Composable
private fun PreviewWindowClosed() {
    NitiGrowTheme {
        WindowStatusBanner(windowExpiresAt = Instant.now().minus(2, ChronoUnit.HOURS))
    }
}
