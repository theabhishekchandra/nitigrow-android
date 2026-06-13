package com.websbaba.nitigrow.presentation.feature.campaigns.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// CampaignRow — one broadcast card in the Broadcasts list.
//
//   SCHEDULED  → turmeric pill + "Sat 14 Jun · 10:00 AM" + meta line
//   COMPLETED  → brand pill + date + chevron + SENT / DELIVERED / READ stats
//   RUNNING    → brand pill + live stats (same layout as completed)
//   DRAFT      → neutral pill on a dashed-border card
//   FAILED     → danger pill; CANCELLED → neutral pill
// ─────────────────────────────────────────────────────────────────────────────

private val CardShape = RoundedCornerShape(16.dp)
private val nf: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

private val scheduleFormat =
    DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault())
private val dayFormat =
    DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH).withZone(ZoneId.systemDefault())

@Composable
fun CampaignRow(campaign: Campaign, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Theme.colors
    val isDraft = campaign.status == CampaignStatus.DRAFT
    val showStats =
        (campaign.status == CampaignStatus.COMPLETED || campaign.status == CampaignStatus.RUNNING) &&
            campaign.sentCount > 0

    val borderModifier =
        if (isDraft) {
            val dashColor = colors.muted3
            Modifier.drawBehind {
                val inset = 0.5.dp.toPx()
                drawRoundRect(
                    color = dashColor,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                )
            }
        } else {
            Modifier.border(1.dp, colors.border, CardShape)
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(colors.card)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusPill(campaign.status)
            campaignDateLabel(campaign)?.let { date ->
                Text(
                    date,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = colors.muted
                )
            }
            if (showStats) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colors.muted2,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            campaign.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showStats) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampaignStat(nf.format(campaign.sentCount), "SENT", colors.ink)
                CampaignStat(
                    "${(campaign.deliveryRate * 100).roundToInt()}%", "DELIVERED", colors.brand
                )
                CampaignStat("${(campaign.readRate * 100).roundToInt()}%", "READ", colors.brand)
            }
        } else {
            Spacer(Modifier.height(3.dp))
            Text(
                campaignMetaLine(campaign),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CampaignStat(value: String, label: String, valueColor: Color) {
    val colors = Theme.colors
    Column {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = colors.muted
        )
    }
}

@Composable
private fun StatusPill(status: CampaignStatus) {
    val colors = Theme.colors
    val (bg, fg) = when (status) {
        CampaignStatus.COMPLETED, CampaignStatus.RUNNING -> colors.brandSoft to colors.brand
        CampaignStatus.SCHEDULED -> colors.turmericSoft to colors.turmericInk
        CampaignStatus.FAILED -> colors.danger.copy(alpha = 0.12f) to colors.danger
        CampaignStatus.DRAFT, CampaignStatus.CANCELLED -> colors.paper2 to colors.ink3
    }
    Text(
        status.name,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

private fun campaignDateLabel(campaign: Campaign): String? = when (campaign.status) {
    CampaignStatus.SCHEDULED -> campaign.scheduledAt?.let(scheduleFormat::format)
    CampaignStatus.DRAFT -> null
    else -> dayFormat.format(campaign.createdAt)
}

private fun campaignMetaLine(campaign: Campaign): String = buildList {
    if (campaign.templateName.isNotBlank()) add(campaign.templateName)
    if (campaign.audienceTags.isNotEmpty()) add(campaign.audienceTags.joinToString(", "))
    add("${nf.format(campaign.audienceSize)} recipients")
}.joinToString(" · ")

// ── Previews ─────────────────────────────────────────────────────────────────

private fun previewCampaign(status: CampaignStatus) = Campaign(
    id = "c1",
    name = "Mango Mithai Festival",
    templateId = "t1",
    templateName = "mango_festival_offer",
    audienceTags = listOf("VIP", "Repeat buyers"),
    audienceSize = 1290,
    status = status,
    scheduledAt = Instant.now().plusSeconds(86_400),
    sentCount = 1240,
    deliveredCount = 1190,
    readCount = 845,
    failedCount = 12,
    createdAt = Instant.now().minusSeconds(86_400)
)

@Preview(showBackground = true)
@Composable
private fun CampaignRowPreview() {
    NitiGrowTheme {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(18.dp)) {
            CampaignRow(previewCampaign(CampaignStatus.SCHEDULED), onClick = {})
            CampaignRow(previewCampaign(CampaignStatus.COMPLETED), onClick = {})
            CampaignRow(previewCampaign(CampaignStatus.DRAFT), onClick = {})
        }
    }
}
