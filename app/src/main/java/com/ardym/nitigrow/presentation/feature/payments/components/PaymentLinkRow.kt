package com.ardym.nitigrow.presentation.feature.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.presentation.feature.payments.SentLinkStatus
import com.ardym.nitigrow.presentation.feature.payments.SentPaymentLink
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkRow — single row in the "Recent payment links" list.
//
//   ┌────────────────────────────────────────────────────────────┐
//   │ ⬤  Priya Sharma                       ₹12,000 │  18 min ago│
//   │ P                                       [PAID]              │
//   └────────────────────────────────────────────────────────────┘
//
// Status pill colour follows the brand semantic palette via Theme.colors:
//   PAID    → success (green)
//   PENDING → warning (amber)
//   EXPIRED → muted   (grey)
//   FAILED  → danger  (red)
//
// Avatar background is picked deterministically from Theme.colors.avatars so a
// given contact always lands on the same warm tone across the app.
// ─────────────────────────────────────────────────────────────────────────────

private val InrFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLinkRow(
    link: SentPaymentLink,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val colors = Theme.colors
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AvatarInitial(name = link.contactName)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = link.contactName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = relativeTime(link.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "₹${InrFormat.format(link.amountInr)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    fontWeight = FontWeight.SemiBold,
                )
                StatusChip(status = link.status)
            }
        }
    }
}

@Composable
private fun AvatarInitial(name: String) {
    val colors = Theme.colors
    val initial = name.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
    val (bg, fg) = colors.avatars[Math.floorMod(name.hashCode(), colors.avatars.size)]
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusChip(status: SentLinkStatus) {
    val colors = Theme.colors
    val tint: Color = when (status) {
        SentLinkStatus.PAID -> colors.success
        SentLinkStatus.PENDING -> colors.warning
        SentLinkStatus.EXPIRED -> colors.muted
        SentLinkStatus.FAILED -> colors.danger
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun relativeTime(instant: Instant): String {
    val seconds = Duration.between(instant, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        seconds < SECONDS_PER_MINUTE -> "just now"
        seconds < SECONDS_PER_MINUTE * MINUTES_PER_HOUR -> "${seconds / SECONDS_PER_MINUTE} min ago"
        seconds < SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY -> {
            val hours = seconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)
            "$hours h ago"
        }
        else -> {
            val days = seconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY)
            "$days d ago"
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "PaymentLinkRow — PAID")
@Composable
private fun PreviewPaymentLinkRowPaid() {
    val sample = SentPaymentLink(
        id = "pl-001",
        contactName = "Priya Sharma",
        amountInr = 12_000,
        status = SentLinkStatus.PAID,
        sentAt = Instant.now().minusSeconds(SECONDS_PER_MINUTE * 18),
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}

@Preview(showBackground = true, name = "PaymentLinkRow — PENDING")
@Composable
private fun PreviewPaymentLinkRowPending() {
    val sample = SentPaymentLink(
        id = "pl-002",
        contactName = "Rahul Verma",
        amountInr = 48_500,
        status = SentLinkStatus.PENDING,
        sentAt = Instant.now().minusSeconds(SECONDS_PER_MINUTE * MINUTES_PER_HOUR * 2),
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}

@Preview(showBackground = true, name = "PaymentLinkRow — FAILED")
@Composable
private fun PreviewPaymentLinkRowFailed() {
    val sample = SentPaymentLink(
        id = "pl-003",
        contactName = "Vikram Singh",
        amountInr = 750,
        status = SentLinkStatus.FAILED,
        sentAt = Instant.now().minusSeconds(SECONDS_PER_MINUTE * MINUTES_PER_HOUR * 9),
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}
