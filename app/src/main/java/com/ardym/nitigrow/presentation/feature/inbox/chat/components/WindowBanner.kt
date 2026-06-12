package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// WindowPill — compact 24h customer-service-window status pill shown in the
// chat header (replaces the old full-width WindowBanner per the Inbox/Chat
// redesign). See docs/phase-3-mobile.md "WhatsApp 24-Hour Messaging Window".
//
//   window open   → turmericSoft / turmericInk  "21h window" / "45m window"
//   window closed → paper2 / ink3               "Window closed"
//   unknown (null expiry — last message was outbound) → renders nothing
//
// The label re-evaluates once a minute; the ticker restarts whenever the
// expiry changes (i.e. a new inbound message reopens the window).
// ─────────────────────────────────────────────────────────────────────────────

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE
private const val TICKER_MILLIS = 60_000L

@Composable
fun WindowPill(windowExpiresAt: Instant?, modifier: Modifier = Modifier) {
    if (windowExpiresAt == null) return

    // Live ticker — recomputes "now" once per minute, keyed to the expiry so
    // switching conversations (or a new inbound message) restarts it cleanly.
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
    val label = when {
        !open -> "Window closed"
        remainingMillis >= MILLIS_PER_HOUR -> "${remainingMillis / MILLIS_PER_HOUR}h window"
        else -> "${(remainingMillis / MILLIS_PER_MINUTE).coerceAtLeast(1L)}m window"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
        fontWeight = FontWeight.Bold,
        color = if (open) Theme.colors.turmericInk else Theme.colors.ink3,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (open) Theme.colors.turmericSoft else Theme.colors.paper2)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "WindowPill — 21h left", showBackground = true)
@Composable
private fun PreviewWindowPillOpen() {
    NitiGrowTheme {
        WindowPill(windowExpiresAt = Instant.now().plus(21, ChronoUnit.HOURS))
    }
}

@Preview(name = "WindowPill — 45m left", showBackground = true)
@Composable
private fun PreviewWindowPillClosingSoon() {
    NitiGrowTheme {
        WindowPill(windowExpiresAt = Instant.now().plus(45, ChronoUnit.MINUTES))
    }
}

@Preview(name = "WindowPill — closed", showBackground = true)
@Composable
private fun PreviewWindowPillClosed() {
    NitiGrowTheme {
        WindowPill(windowExpiresAt = Instant.now().minus(2, ChronoUnit.HOURS))
    }
}
