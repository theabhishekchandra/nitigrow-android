package com.websbaba.nitigrow.presentation.feature.campaigns.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// CampaignDetailScreen — live report for one broadcast.
//
//   ◀  Mango Mithai Festival            ── Fraunces 19sp + meta subtitle
//   ┌─ espresso hero ─────────────────┐
//   │ TOTAL SENT                       │
//   │ 1,240            (Fraunces 40sp) │
//   │ 96%        71%        (gold)     │
//   │ DELIVERED  READ                  │
//   └──────────────────────────────────┘
//   ┌─ DELIVERY FUNNEL ── brand / turmeric (+ danger when failures) bars ─┐
//   ┌─ AUDIENCE ──────── segments · recipients · schedule ───────────────┐
//   [ Cancel broadcast ]  (scheduled / running only)
// ─────────────────────────────────────────────────────────────────────────────

private val nf: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))
private val dayFormat =
    DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH).withZone(ZoneId.systemDefault())
private val scheduleFormat =
    DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault())

private val SectionShape = RoundedCornerShape(16.dp)

@Composable
fun CampaignDetailScreen(
    onBack: () -> Unit,
    viewModel: CampaignDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Theme.colors
    val c = state.campaign

    Scaffold(containerColor = colors.paper) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.ink,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        c?.name ?: "Broadcast",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 19.sp),
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    c?.let {
                        Text(
                            campaignMeta(it),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = colors.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (c == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.error?.let { ErrorBanner(message = it) } ?: Text(
                        "Loading…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HeroCard(c)
                    FunnelCard(c)
                    AudienceCard(c)
                    if (c.status == CampaignStatus.SCHEDULED || c.status == CampaignStatus.RUNNING) {
                        OutlinedButton(
                            onClick = viewModel::onCancel,
                            enabled = !state.cancelling,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = colors.card,
                                contentColor = colors.danger
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                "Cancel broadcast",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    state.error?.let { ErrorBanner(message = it) }
                }
            }
        }
    }
}

private fun campaignMeta(c: Campaign): String {
    val statusPart = when (c.status) {
        CampaignStatus.SCHEDULED ->
            c.scheduledAt?.let { "Scheduled ${scheduleFormat.format(it)}" } ?: "Scheduled"
        CampaignStatus.COMPLETED -> "Completed ${dayFormat.format(c.createdAt)}"
        CampaignStatus.RUNNING -> "Sending now"
        CampaignStatus.DRAFT -> "Draft"
        CampaignStatus.FAILED -> "Failed"
        CampaignStatus.CANCELLED -> "Cancelled"
    }
    return listOf(c.templateName, statusPart)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
}

private fun pctOfSent(count: Long, sent: Long): Int =
    if (sent > 0) (count * 100f / sent).roundToInt() else 0

// ── Espresso hero ────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(c: Campaign) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.sidebarBg)
            .padding(18.dp)
    ) {
        Text(
            "TOTAL SENT",
            style = MaterialTheme.typography.labelSmall,
            color = colors.sidebarInk.copy(alpha = 0.7f)
        )
        Text(
            nf.format(c.sentCount),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp, lineHeight = 46.sp),
            color = colors.sidebarInk,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(top = 14.dp)
        ) {
            HeroStat("${pctOfSent(c.deliveredCount, c.sentCount)}%", "DELIVERED")
            HeroStat("${pctOfSent(c.readCount, c.sentCount)}%", "READ")
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String) {
    val colors = Theme.colors
    Column {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
            fontWeight = FontWeight.Bold,
            color = colors.sidebarTextActive
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
            color = colors.sidebarInk.copy(alpha = 0.55f)
        )
    }
}

// ── Delivery funnel ──────────────────────────────────────────────────────────

@Composable
private fun FunnelCard(c: Campaign) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SectionShape)
            .background(colors.card)
            .border(1.dp, colors.border, SectionShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "DELIVERY FUNNEL",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        FunnelRow("Delivered", c.deliveredCount, c.sentCount, colors.brand)
        FunnelRow("Read", c.readCount, c.sentCount, colors.turmeric)
        if (c.failedCount > 0) {
            FunnelRow("Failed", c.failedCount, c.sentCount, colors.danger)
        }
    }
}

@Composable
private fun FunnelRow(label: String, count: Long, sent: Long, fill: Color) {
    val colors = Theme.colors
    val fraction = if (sent > 0) (count.toFloat() / sent).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = colors.ink
            )
            Text(
                nf.format(count),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.paper2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(fill)
            )
        }
    }
}

// ── Audience ─────────────────────────────────────────────────────────────────

@Composable
private fun AudienceCard(c: Campaign) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SectionShape)
            .background(colors.card)
            .border(1.dp, colors.border, SectionShape)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "AUDIENCE",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 14.dp)
        )
        Spacer(Modifier.height(2.dp))
        InfoRow("Segments", c.audienceTags.joinToString(", ").ifBlank { "—" })
        HorizontalDivider(color = colors.border2)
        InfoRow("Recipients", nf.format(c.audienceSize))
        c.scheduledAt?.let {
            HorizontalDivider(color = colors.border2)
            InfoRow("Scheduled for", scheduleFormat.format(it))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = Theme.colors
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
