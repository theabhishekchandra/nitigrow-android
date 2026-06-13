package com.websbaba.nitigrow.presentation.feature.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.presentation.feature.payments.SentLinkStatus
import com.websbaba.nitigrow.presentation.feature.payments.SentPaymentLink
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkRow — one payment link in the list (design: Payments screen).
//
//   ┌─────────────────────────────────────────────────────────────┐
//   │ ┌────┐  ₹6,800                          (Pending)   [⧉]    │
//   │ │ ₹  │  Rajesh Verma · Today, 9:20 AM                       │
//   │ └────┘                                                      │
//   └─────────────────────────────────────────────────────────────┘
//
// • 42dp turmericSoft square (radius 13) with a Fraunces ₹ glyph.
// • Status pill: Paid → brandSoft/brand · Pending → turmericSoft/turmericInk
//   · Expired → paper2/muted · Failed → danger soft/danger.
// • 32dp bordered copy button, shown only when the link has a real URL.
// ─────────────────────────────────────────────────────────────────────────────

private val InrFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

private val TimeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private val DayFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
private val DateFmt = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

@Composable
fun PaymentLinkRow(
    link: SentPaymentLink,
    modifier: Modifier = Modifier,
    onCopyLink: (String) -> Unit = {},
) {
    val colors = Theme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(15.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RupeeGlyph()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "₹${InrFormat.format(link.amountInr)}",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${link.contactName} · ${linkWhen(link.sentAt)}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.5.sp,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        StatusPill(status = link.status)
        link.linkUrl?.let { url ->
            CopyButton(onClick = { onCopyLink(url) })
        }
    }
}

@Composable
private fun RupeeGlyph() {
    val colors = Theme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(colors.turmericSoft),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "₹",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.turmericInk,
        )
    }
}

@Composable
private fun StatusPill(status: SentLinkStatus) {
    val colors = Theme.colors
    val (bg, fg, label) = when (status) {
        SentLinkStatus.PAID -> Triple(colors.brandSoft, colors.brand, "Paid")
        SentLinkStatus.PENDING -> Triple(colors.turmericSoft, colors.turmericInk, "Pending")
        SentLinkStatus.EXPIRED -> Triple(colors.paper2, colors.muted, "Expired")
        SentLinkStatus.FAILED -> Triple(colors.danger.copy(alpha = 0.12f), colors.danger, "Failed")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun CopyButton(onClick: () -> Unit) {
    val colors = Theme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "Copy payment link",
            tint = colors.muted,
            modifier = Modifier.size(14.dp),
        )
    }
}

/**
 * "Today, 9:20 AM" → same day · "Tue, 4:12 PM" → last 7 days · "28 May" → older.
 * Mirrors the prototype's `when` strings.
 */
private fun linkWhen(sentAt: Instant): String {
    val zone = ZoneId.systemDefault()
    val dateTime = sentAt.atZone(zone)
    val date = dateTime.toLocalDate()
    val today = LocalDate.now(zone)
    return when {
        date == today -> "Today, ${TimeFmt.format(dateTime)}"
        date.isAfter(today.minusDays(7)) -> "${DayFmt.format(dateTime)}, ${TimeFmt.format(dateTime)}"
        else -> DateFmt.format(date)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "PaymentLinkRow — PAID")
@Composable
private fun PreviewPaymentLinkRowPaid() {
    val sample = SentPaymentLink(
        id = "pl-001",
        contactName = "Kavita Reddy",
        amountInr = 2_450,
        status = SentLinkStatus.PAID,
        sentAt = Instant.now().minusSeconds(60L * 60 * 26),
        linkUrl = "https://nitigrow.in/pay/pl-001",
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}

@Preview(showBackground = true, name = "PaymentLinkRow — PENDING")
@Composable
private fun PreviewPaymentLinkRowPending() {
    val sample = SentPaymentLink(
        id = "pl-002",
        contactName = "Rajesh Verma",
        amountInr = 6_800,
        status = SentLinkStatus.PENDING,
        sentAt = Instant.now().minusSeconds(60L * 18),
        linkUrl = "https://nitigrow.in/pay/pl-002",
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}

@Preview(showBackground = true, name = "PaymentLinkRow — EXPIRED, no URL")
@Composable
private fun PreviewPaymentLinkRowExpired() {
    val sample = SentPaymentLink(
        id = "pl-003",
        contactName = "Deepak Joshi",
        amountInr = 3_200,
        status = SentLinkStatus.EXPIRED,
        sentAt = Instant.now().minusSeconds(60L * 60 * 24 * 30),
        linkUrl = null,
    )
    NitiGrowTheme { PaymentLinkRow(link = sample) }
}
