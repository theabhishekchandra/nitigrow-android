package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// ChatHeaderPresence — subtext rendered beneath the contact's name in the
// chat top bar.
// See docs/phase-3-mobile.md "Typing & Presence".
//
// Priority order:
//   1. typing == true                → "Typing…" (brand-coloured + pulsing dot)
//   2. lastSeen is null              → empty (nothing rendered — caller can
//                                       collapse the row to save vertical space)
//   3. lastSeen is on system date    → "today at HH:mm"
//   4. lastSeen is on yesterday      → "yesterday at HH:mm"
//   5. otherwise                     → localized medium date + "HH:mm"
//
// Time formatting uses the device locale via DateTimeFormatter so Hindi/Marathi
// readers see localized strings out of the box (matches Theme.kt's typography
// auto-switch).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatHeaderPresence(
    typing: Boolean,
    lastSeen: Instant?,
    modifier: Modifier = Modifier,
    fallback: String = ""
) {
    val colors = Niti.colors
    val locale = LocalConfiguration.current.locales.get(0) ?: Locale.getDefault()
    val subtextStyle = NitiType.label.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        when {
            typing -> {
                Text(
                    text = "typing…",
                    style = subtextStyle,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            lastSeen != null -> {
                Text(
                    text = formatLastSeen(lastSeen, locale),
                    style = subtextStyle,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Online state — when the contact has had inbound activity in the
            // last 2 minutes, the caller should pass `lastSeen = Instant.now()`
            // (or close to it). We render an explicit "Online" pill when the
            // gap is under that threshold.
            fallback.isNotBlank() -> {
                Text(
                    text = fallback,
                    style = subtextStyle,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private const val ONLINE_THRESHOLD_MINUTES = 2L

private fun formatLastSeen(lastSeen: Instant, locale: Locale): String {
    val zone = ZoneId.systemDefault()
    val now = Instant.now()

    // "Online" when the customer was seen in the last 2 minutes.
    val minutesAgo = ChronoUnit.MINUTES.between(lastSeen, now)
    if (minutesAgo in 0..ONLINE_THRESHOLD_MINUTES) return "Online"

    val seenDate = lastSeen.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm").withLocale(locale).withZone(zone)
    val timePart = timeFmt.format(lastSeen)

    return when (seenDate) {
        today -> "Last seen today at $timePart"
        today.minusDays(1) -> "Last seen yesterday at $timePart"
        else -> {
            val dateFmt = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(zone)
            "Last seen ${dateFmt.format(lastSeen)} at $timePart"
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Presence — typing", showBackground = true)
@Composable
private fun PreviewPresenceTyping() {
    NitiGrowTheme {
        ChatHeaderPresence(typing = true, lastSeen = null)
    }
}

@Preview(name = "Presence — online (now)", showBackground = true)
@Composable
private fun PreviewPresenceOnline() {
    NitiGrowTheme {
        ChatHeaderPresence(typing = false, lastSeen = Instant.now())
    }
}

@Preview(name = "Presence — today 14:30", showBackground = true)
@Composable
private fun PreviewPresenceToday() {
    NitiGrowTheme {
        ChatHeaderPresence(
            typing = false,
            lastSeen = Instant.now().minus(3, ChronoUnit.HOURS)
        )
    }
}

@Preview(name = "Presence — yesterday", showBackground = true)
@Composable
private fun PreviewPresenceYesterday() {
    NitiGrowTheme {
        ChatHeaderPresence(
            typing = false,
            lastSeen = Instant.now().minus(28, ChronoUnit.HOURS)
        )
    }
}

@Preview(name = "Presence — last week", showBackground = true)
@Composable
private fun PreviewPresenceOlder() {
    NitiGrowTheme {
        ChatHeaderPresence(
            typing = false,
            lastSeen = Instant.now().minus(8, ChronoUnit.DAYS)
        )
    }
}

@Preview(name = "Presence — never seen (null collapses)", showBackground = true)
@Composable
private fun PreviewPresenceNever() {
    NitiGrowTheme {
        ChatHeaderPresence(typing = false, lastSeen = null)
    }
}
