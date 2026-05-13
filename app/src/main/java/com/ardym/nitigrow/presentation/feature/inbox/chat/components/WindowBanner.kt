package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// WindowBanner — live-counting reminder of the WhatsApp messaging window.
// See docs/phase-3-mobile.md "WhatsApp 24-Hour Messaging Window".
//
//   • H24 = standard customer-initiated session window (24h after last inbound).
//   • H72 = extended Click-to-WhatsApp Ads window (72h, banner says "Ad window").
//
// State machine (driven purely by remaining millis vs `Instant.now()`):
//   > 1h        → healthy / green-tinted brand surface
//   ≤ 1h, > 15m → warning / turmeric (orange) surface
//   ≤ 15m, > 0  → critical / danger-tinted surface
//   ≤ 0         → expired / locked surface with [Send Template] CTA
//
// The remaining-time text refreshes every 60s via a LaunchedEffect keyed to
// `windowExpiresAt`, so swapping conversations triggers an immediate recompute.
// ─────────────────────────────────────────────────────────────────────────────

/** Distinguishes the standard 24h session window from the 72h CTWA-ad window. */
enum class WindowType { H24, H72 }

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE
private const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR
private const val TICKER_MILLIS = 60_000L
private const val WARNING_THRESHOLD_MILLIS = MILLIS_PER_HOUR          // < 1h
private const val CRITICAL_THRESHOLD_MILLIS = 15L * MILLIS_PER_MINUTE // < 15m

private enum class BannerLevel { Healthy, Warning, Critical, Expired }

@Composable
fun WindowBanner(
    windowExpiresAt: Instant,
    windowType: WindowType,
    onSendTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Live ticker — recomputes "now" once per minute. Keyed to the expiry so
    // navigating between conversations restarts the timer cleanly.
    var nowMillis by remember(windowExpiresAt) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(windowExpiresAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(TICKER_MILLIS)
        }
    }

    val remainingMillis = windowExpiresAt.toEpochMilli() - nowMillis
    val level = when {
        remainingMillis <= 0L -> BannerLevel.Expired
        remainingMillis < CRITICAL_THRESHOLD_MILLIS -> BannerLevel.Critical
        remainingMillis < WARNING_THRESHOLD_MILLIS -> BannerLevel.Warning
        else -> BannerLevel.Healthy
    }

    val colors = Theme.colors
    val (bg, ink, accent) = when (level) {
        BannerLevel.Healthy -> Triple(colors.brandSoft, colors.brandInk, colors.brand)
        BannerLevel.Warning -> Triple(colors.turmericSoft, colors.ink2, colors.warning)
        BannerLevel.Critical -> Triple(colors.accentSoft, colors.danger, colors.danger)
        BannerLevel.Expired -> Triple(colors.accentSoft, colors.danger, colors.danger)
    }

    val prefix = when (windowType) {
        WindowType.H24 -> "Window closes in"
        WindowType.H72 -> "Ad window closes in"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(width = 1.dp, color = accent.copy(alpha = 0.35f), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Coloured leading bar — quick visual cue for the level.
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )

        if (level == BannerLevel.Expired) {
            Text(
                text = lockedMessage(windowType),
                style = MaterialTheme.typography.bodyMedium,
                color = ink,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            FilledTonalButton(
                onClick = onSendTemplate,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.brand,
                    contentColor = colors.paper
                )
            ) {
                Text("Send Template", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Text(
                text = liveMessage(level, prefix, remainingMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = ink,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun liveMessage(level: BannerLevel, prefix: String, remainingMillis: Long): String {
    val pretty = formatRemaining(remainingMillis)
    return when (level) {
        BannerLevel.Healthy -> "⏱ $prefix: $pretty"
        BannerLevel.Warning -> "⚠️ $prefix: $pretty — send a template to keep conversation open"
        BannerLevel.Critical -> "🔴 Window closing soon — $pretty left"
        BannerLevel.Expired -> "" // handled by lockedMessage
    }
}

private fun lockedMessage(windowType: WindowType): String = when (windowType) {
    WindowType.H24 -> "🔒 24-hour window closed. Send a template to start the conversation."
    WindowType.H72 -> "🔒 72-hour ad window closed. Send a template to start the conversation."
}

/**
 * Formats remaining duration WhatsApp-style:
 *   ≥ 1d → "2d 6h"
 *   ≥ 1h → "6h 30m"
 *   ≥ 1m → "45 minutes" / "12 minutes"
 *   < 1m → "less than a minute"
 */
private fun formatRemaining(millis: Long): String {
    if (millis <= 0L) return "0m"
    val days = millis / MILLIS_PER_DAY
    val hours = (millis % MILLIS_PER_DAY) / MILLIS_PER_HOUR
    val minutes = (millis % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "$minutes minutes"
        else -> "less than a minute"
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "WindowBanner — healthy (6h 30m)", showBackground = true)
@Composable
private fun PreviewWindowBannerHealthy() {
    NitiGrowTheme {
        WindowBanner(
            windowExpiresAt = Instant.now().plus(6, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES),
            windowType = WindowType.H24,
            onSendTemplate = {}
        )
    }
}

@Preview(name = "WindowBanner — warning (45m)", showBackground = true)
@Composable
private fun PreviewWindowBannerWarning() {
    NitiGrowTheme {
        WindowBanner(
            windowExpiresAt = Instant.now().plus(45, ChronoUnit.MINUTES),
            windowType = WindowType.H24,
            onSendTemplate = {}
        )
    }
}

@Preview(name = "WindowBanner — critical (12m)", showBackground = true)
@Composable
private fun PreviewWindowBannerCritical() {
    NitiGrowTheme {
        WindowBanner(
            windowExpiresAt = Instant.now().plus(12, ChronoUnit.MINUTES),
            windowType = WindowType.H24,
            onSendTemplate = {}
        )
    }
}

@Preview(name = "WindowBanner — expired", showBackground = true)
@Composable
private fun PreviewWindowBannerExpired() {
    NitiGrowTheme {
        WindowBanner(
            windowExpiresAt = Instant.now().minus(2, ChronoUnit.MINUTES),
            windowType = WindowType.H24,
            onSendTemplate = {}
        )
    }
}

@Preview(name = "WindowBanner — 72h ad window healthy", showBackground = true)
@Composable
private fun PreviewWindowBannerAdHealthy() {
    NitiGrowTheme {
        WindowBanner(
            windowExpiresAt = Instant.now().plus(2, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS),
            windowType = WindowType.H72,
            onSendTemplate = {}
        )
    }
}

@Suppress("UnusedPrivateProperty")
private val PreviewBackgroundColor: Color = Color.Transparent
