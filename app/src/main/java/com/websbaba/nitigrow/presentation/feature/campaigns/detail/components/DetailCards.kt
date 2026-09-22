package com.websbaba.nitigrow.presentation.feature.campaigns.detail.components

import androidx.compose.foundation.layout.ColumnScope
import com.websbaba.nitigrow.presentation.components.NitiCard
import com.websbaba.nitigrow.core.util.ShortDayTimeFormat
import com.websbaba.nitigrow.core.util.TimeOfDayFormat
import com.websbaba.nitigrow.core.util.DayFormat
import com.websbaba.nitigrow.core.util.formatIndian
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.core.ui.theme.BubbleInShape
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt


private val CardShape = RoundedCornerShape(24.dp)

/** "Starts in 19 days", "Starts in 2h 5m", "Starts in 12m" — or "Starting now" once due. */
internal fun startsInLabel(now: Instant, at: Instant): String {
    val remaining = Duration.between(now, at)
    if (remaining.isNegative || remaining.isZero) return "Starting now"
    val days = remaining.toDays()
    val hours = remaining.toHours() % 24
    val minutes = remaining.toMinutes() % 60
    return when {
        days >= 1 -> "Starts in $days ${if (days == 1L) "day" else "days"}"
        hours >= 1 && minutes > 0 -> "Starts in ${hours}h ${minutes}m"
        hours >= 1 -> "Starts in ${hours}h"
        else -> "Starts in ${minutes.coerceAtLeast(1L)}m"
    }
}

/** Share of [sent] that [count] represents, as a whole percent (0 when nothing was sent). */
internal fun percentOfSent(count: Long, sent: Long): Int =
    if (sent > 0) (count * 100f / sent).roundToInt() else 0

// ── Heroes ───────────────────────────────────────────────────────────────────

/** "TOTAL SENT 820 of 1,240 contacts", optionally with a live progress bar. */
@Composable
fun SentHeroCard(campaign: Campaign, showProgress: Boolean, caption: String, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val ringColor = colors.onHero.copy(alpha = 0.15f)
    val progress = if (campaign.audienceSize > 0)
        (campaign.sentCount.toFloat() / campaign.audienceSize).coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.hero)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = ringColor,
                radius = 85.dp.toPx(),
                center = Offset(size.width + 40.dp.toPx() - 85.dp.toPx(), (-50).dp.toPx() + 85.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "TOTAL SENT",
                style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                color = colors.onHero.copy(alpha = 0.85f)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = formatIndian(campaign.sentCount),
                    style = NitiType.numberXl.copy(fontSize = 52.sp, lineHeight = 56.sp),
                    color = colors.onHero
                )
                Text(
                    text = "of ${formatIndian(campaign.audienceSize)} contacts",
                    style = NitiType.body.copy(fontSize = 16.sp),
                    color = colors.onHero.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (showProgress) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.onHero.copy(alpha = 0.22f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.onHero)
                    )
                }
            }
            Text(
                text = caption,
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = colors.onHero.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

/** Amber hero for a broadcast that hasn't started: date, time and a countdown. */
@Composable
fun ScheduledHeroCard(scheduledAt: Instant?, now: Instant, modifier: Modifier = Modifier) {
    val tone = Niti.colors.secondaryTone
    val blobColor = Niti.colors.chartSecondary.copy(alpha = 0.3f)
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(tone.container)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = blobColor,
                radius = 85.dp.toPx(),
                center = Offset(size.width + 40.dp.toPx() - 85.dp.toPx(), (-50).dp.toPx() + 85.dp.toPx())
            )
        }
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "SCHEDULED FOR",
                style = NitiType.caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                color = tone.onContainer
            )
            if (scheduledAt == null) {
                Text(
                    text = "Time not set",
                    style = NitiType.display.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                    color = tone.onContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = DayFormat.format(scheduledAt),
                    style = NitiType.display.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
                    color = tone.onContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = TimeOfDayFormat.format(scheduledAt),
                    style = NitiType.body.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    color = tone.onContainer,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Icon(NitiIcons.Clock, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(18.dp))
                    Text(
                        text = startsInLabel(now, scheduledAt),
                        style = NitiType.bodyCompact,
                        color = tone.onContainer
                    )
                }
            }
        }
    }
}

/** Neutral hero for a draft that has no schedule yet. */
@Composable
fun DraftHeroCard(modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceHigh)
            .padding(22.dp)
    ) {
        Text(
            text = "DRAFT",
            style = NitiType.caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
            color = colors.onSurfaceVariant
        )
        Text(
            text = "Not scheduled yet",
            style = NitiType.display.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
            color = colors.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun completedCaption(campaign: Campaign): String =
    "Created ${ShortDayTimeFormat.format(campaign.createdAt)}"

// ── Cards ────────────────────────────────────────────────────────────────────

@Composable
private fun DetailCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    NitiCard(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp), content = content)
}

@Composable
fun DeliveryFunnelCard(campaign: Campaign, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val sent = campaign.sentCount
    DetailCard(modifier) {
        Text(text = "Delivery funnel", style = NitiType.titleUi, color = colors.onSurface)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
            FunnelRow("Sent", sent, sent, colors.outline)
            FunnelRow("Delivered", campaign.deliveredCount, sent, colors.chartPrimary)
            FunnelRow("Read", campaign.readCount, sent, colors.chartSecondary)
            if (campaign.failedCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.errorTone.container)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(NitiIcons.Warning, contentDescription = null, tint = colors.errorTone.onContainer, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Failed",
                        style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Medium),
                        color = colors.errorTone.onContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatIndian(campaign.failedCount),
                        style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Bold),
                        color = colors.errorTone.onContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun FunnelRow(label: String, count: Long, sent: Long, fill: Color) {
    val colors = Niti.colors
    val percent = percentOfSent(count, sent)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, style = NitiType.bodyCompact.copy(fontWeight = FontWeight.Medium), color = colors.onSurface)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.onSurface)) { append(formatIndian(count)) }
                    append(" · $percent%")
                },
                style = NitiType.bodyCompact,
                color = colors.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(fill)
            )
        }
    }
}

/** Big recipient count — shown before a broadcast has sent anything. */
@Composable
fun RecipientsCard(count: Int, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    DetailCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Recipients", style = NitiType.titleUi, color = colors.onSurface, modifier = Modifier.weight(1f))
            Text(text = formatIndian(count), style = NitiType.title.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AudienceCard(tags: List<String>, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    DetailCard(modifier) {
        Text(text = "Audience", style = NitiType.titleUi, color = colors.onSurface)
        if (tags.isEmpty()) {
            Text(
                text = "No segment filter",
                style = NitiType.bodyCompact,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                tags.forEach { tag ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .heightIn(min = 28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primaryTone.container)
                            .padding(horizontal = 9.dp)
                    ) {
                        Text(
                            text = tag,
                            style = NitiType.label.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
                            color = colors.primaryTone.onContainer
                        )
                    }
                }
            }
        }
    }
}

/** Template name plus, when cached, a preview of its body in a chat-style bubble. */
@Composable
fun TemplateCard(templateName: String, template: Template?, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    DetailCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Template", style = NitiType.titleUi, color = colors.onSurface)
            Text(
                text = templateName.ifBlank { "—" },
                style = NitiType.label,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        if (template != null && template.body.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(colors.primaryTone.container)
                    .padding(14.dp)
            ) {
                Text(
                    text = template.body,
                    style = NitiType.bodyCompact.copy(lineHeight = 21.sp),
                    color = colors.onBubbleIn,
                    modifier = Modifier
                        .widthIn(max = 290.dp)
                        .clip(BubbleInShape)
                        .background(colors.surface)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}
