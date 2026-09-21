package com.websbaba.nitigrow.presentation.feature.campaigns.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// CampaignRow — one broadcast card in the Broadcasts list.
//
//   RUNNING    → "Sending now" pill + progress bar + "820 of 1,240 sent · 66%"
//   SCHEDULED  → "Scheduled" pill + clock + "Sat, 10 Oct · 9:00 AM"
//   COMPLETED  → "Completed" pill + Delivered / Read percentages
//   DRAFT      → neutral pill + "Not scheduled yet"
//   FAILED     → error pill + failed count; CANCELLED → neutral pill
// ─────────────────────────────────────────────────────────────────────────────

private val nf: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

private val scheduleFormat =
    DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault())

/** Fraction of the audience already sent, clamped to 0..1. */
internal fun campaignProgress(campaign: Campaign): Float =
    if (campaign.audienceSize <= 0) 0f
    else (campaign.sentCount.toFloat() / campaign.audienceSize).coerceIn(0f, 1f)

@Composable
fun CampaignRow(campaign: Campaign, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceLow)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaign.name,
                    style = NitiType.titleUi,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = campaignMetaLine(campaign),
                    style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CampaignStatusPill(campaign.status)
        }
        CampaignDetail(campaign)
    }
}

@Composable
private fun CampaignDetail(campaign: Campaign) {
    val colors = Niti.colors
    val bodyStyle = NitiType.label.copy(fontWeight = FontWeight.Normal)
    when (campaign.status) {
        CampaignStatus.RUNNING -> {
            val progress = campaignProgress(campaign)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.track)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.primary)
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${nf.format(campaign.sentCount)} of ${nf.format(campaign.audienceSize)} sent",
                        style = bodyStyle,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = bodyStyle.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface
                    )
                }
            }
        }
        CampaignStatus.SCHEDULED -> IconLine(
            icon = NitiIcons.Clock,
            text = campaign.scheduledAt?.let(scheduleFormat::format) ?: "Time not set"
        )
        CampaignStatus.COMPLETED -> Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            RateText("Delivered", campaign.deliveryRate)
            RateText("Read", campaign.readRate)
        }
        CampaignStatus.DRAFT -> IconLine(icon = NitiIcons.Pencil, text = "Not scheduled yet")
        CampaignStatus.FAILED -> IconLine(
            icon = NitiIcons.Warning,
            text = if (campaign.failedCount > 0) "${nf.format(campaign.failedCount)} messages failed" else "Sending failed"
        )
        CampaignStatus.CANCELLED -> IconLine(icon = NitiIcons.Close, text = "Cancelled before sending")
    }
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    val colors = Niti.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = NitiType.label.copy(fontWeight = FontWeight.Normal),
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun RateText(label: String, rate: Float) {
    val colors = Niti.colors
    Text(
        text = buildAnnotatedString {
            append("$label ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) {
                append("${(rate * 100).roundToInt()}%")
            }
        },
        style = NitiType.label.copy(fontWeight = FontWeight.Normal),
        color = colors.onSurfaceVariant
    )
}

/** Coloured status chip shared by the list card and the detail header. */
@Composable
fun CampaignStatusPill(status: CampaignStatus, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val (label, icon, tone) = when (status) {
        CampaignStatus.RUNNING -> Triple("Sending now", NitiIcons.Bolt, colors.infoTone)
        CampaignStatus.SCHEDULED -> Triple("Scheduled", NitiIcons.Clock, colors.secondaryTone)
        CampaignStatus.COMPLETED -> Triple("Completed", NitiIcons.Check, colors.primaryTone)
        CampaignStatus.FAILED -> Triple("Failed", NitiIcons.Warning, colors.errorTone)
        CampaignStatus.DRAFT -> Triple("Draft", null, NitiTone(colors.surfaceHigh, colors.onSurfaceVariant))
        CampaignStatus.CANCELLED -> Triple("Cancelled", null, NitiTone(colors.surfaceHigh, colors.onSurfaceVariant))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tone.container)
            .padding(horizontal = 9.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(14.dp))
        }
        Text(
            text = label,
            style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
            color = tone.onContainer,
            maxLines = 1
        )
    }
}

private fun campaignMetaLine(campaign: Campaign): String = buildList {
    if (campaign.templateName.isNotBlank()) add(campaign.templateName)
    add("${nf.format(campaign.audienceSize)} contacts")
}.joinToString(" · ")

// ── Previews ─────────────────────────────────────────────────────────────────

private fun previewCampaign(status: CampaignStatus) = Campaign(
    id = "c1",
    name = "Diwali Early Bird",
    templateId = "t1",
    templateName = "diwali_offer_2026",
    audienceTags = listOf("VIP"),
    audienceSize = 1240,
    status = status,
    scheduledAt = Instant.now().plusSeconds(86_400),
    sentCount = 820,
    deliveredCount = 787,
    readCount = 559,
    failedCount = 12,
    createdAt = Instant.now().minusSeconds(86_400)
)

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun CampaignRowPreview() {
    NitiGrowTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            CampaignStatus.entries.forEach { CampaignRow(previewCampaign(it), onClick = {}) }
        }
    }
}
