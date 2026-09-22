package com.websbaba.nitigrow.presentation.feature.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.R
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlin.math.roundToInt

// ── Header ───────────────────────────────────────────────────────────────────

/** Brand mark, business name and the notifications bell. */
@Composable
fun HomeHeader(
    businessName: String?,
    showUnreadDot: Boolean,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .padding(start = 20.dp, end = 8.dp, top = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                .background(colors.logoTile, RoundedCornerShape(14.dp)),
        ) {
            // Same mark the splash screen uses.
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = "NitiGrow logo",
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = businessName.orEmpty(),
            style = NitiType.label,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBellClick),
        ) {
            Icon(
                imageVector = NitiIcons.Bell,
                contentDescription = if (showUnreadDot) "Notifications, unread" else "Notifications",
                tint = colors.onSurface,
                modifier = Modifier.size(24.dp),
            )
            if (showUnreadDot) {
                // Badge dot ringed in the page colour so it reads against the bell.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 11.dp)
                        .size(12.dp)
                        .background(colors.surface, CircleShape)
                        .padding(2.dp)
                        .background(colors.badge, CircleShape),
                )
            }
        }
    }
}

/** "Namaste, Anita" + a one-line status about waiting replies. */
@Composable
fun HomeGreeting(firstName: String?, pendingReplies: Int, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Column(modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)) {
        Text(
            text = firstName?.let { "Namaste, $it" } ?: "Namaste",
            style = NitiType.display,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = when (pendingReplies) {
                0 -> "You're all caught up."
                1 -> "You have 1 reply waiting."
                else -> "You have $pendingReplies replies waiting."
            },
            style = NitiType.body,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── Quick actions ────────────────────────────────────────────────────────────

/** Four tonal shortcut tiles: Broadcast, Contact, Leads, Pay link. */
@Composable
fun QuickActionRow(
    onBroadcast: () -> Unit,
    onContact: () -> Unit,
    onLeads: () -> Unit,
    onPayLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    Row(modifier = modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 4.dp)) {
        QuickAction("Broadcast", NitiIcons.Megaphone, colors.primaryTone, onBroadcast, Modifier.weight(1f))
        QuickAction("Contact", NitiIcons.Contacts, colors.secondaryTone, onContact, Modifier.weight(1f))
        QuickAction("Leads", NitiIcons.Leads, colors.tertiaryTone, onLeads, Modifier.weight(1f))
        QuickAction("Pay link", NitiIcons.Rupee, colors.infoTone, onPayLink, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    tone: NitiTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 64.dp, height = 56.dp)
                .background(tone.container, RoundedCornerShape(20.dp)),
        ) {
            Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(26.dp))
        }
        Text(
            text = label,
            style = NitiType.label.copy(lineHeight = 16.sp),
            color = Niti.colors.onSurface,
            maxLines = 1,
        )
    }
}

// ── Revenue hero ─────────────────────────────────────────────────────────────

@Composable
fun RevenueHero(
    amount: String,
    onSendPaymentLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(28.dp)
    val ringColor = colors.onHero.copy(alpha = 0.14f)
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(colors.hero),
    ) {
        // Three concentric hairline rings anchored top-right, as in the board.
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width - 50.dp.toPx(), 40.dp.toPx())
            listOf(110.dp, 76.dp, 42.dp).forEach { r ->
                drawCircle(color = ringColor, radius = r.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 20.dp),
        ) {
            Text(
                text = "Revenue · last 30 days",
                style = NitiType.label.copy(lineHeight = 20.sp),
                color = colors.onHero.copy(alpha = 0.88f),
            )
            Text(text = amount, style = NitiType.numberXl, color = colors.onHero, maxLines = 1)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.onHero.copy(alpha = 0.18f))
                    .clickable(role = Role.Button, onClick = onSendPaymentLink)
                    .padding(horizontal = 18.dp),
            ) {
                Icon(NitiIcons.Rupee, contentDescription = null, tint = colors.onHero, modifier = Modifier.size(18.dp))
                Text(
                    text = "Send payment link",
                    style = NitiType.label.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onHero,
                )
            }
        }
    }
}

// ── Snapshot tiles ───────────────────────────────────────────────────────────

/** Side-by-side "Conversations today" and "Pending replies" tiles. */
@Composable
fun SnapshotTiles(
    conversationsToday: String,
    pendingReplies: String,
    onOpenInbox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
    ) {
        SnapshotTile(
            value = conversationsToday,
            label = "Conversations today",
            icon = NitiIcons.Chat,
            container = colors.surfaceLow,
            content = colors.onSurface,
            onClick = onOpenInbox,
            modifier = Modifier.weight(1f),
        )
        SnapshotTile(
            value = pendingReplies,
            label = "Pending replies",
            icon = NitiIcons.Clock,
            container = colors.tertiaryTone.container,
            content = colors.tertiaryTone.onContainer,
            onClick = onOpenInbox,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SnapshotTile(
    value: String,
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Icon(NitiIcons.ArrowUpRight, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(text = value, style = NitiType.numberLg, color = content, maxLines = 1)
            Text(text = label, style = NitiType.label, color = content.copy(alpha = 0.9f))
        }
    }
}

// ── Engagement ───────────────────────────────────────────────────────────────

@Composable
fun EngagementCard(deliveryRate: Float, readRate: Float, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceLow)
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Engagement", style = NitiType.titleUi, color = colors.onSurface)
            Text(
                text = "Last 30 days",
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp)) {
            RingStat("Delivery rate", deliveryRate, colors.chartPrimary, Modifier.weight(1f))
            RingStat("Read rate", readRate, colors.chartSecondary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RingStat(label: String, rate: Float, color: Color, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val target = rate.coerceIn(0f, 1f)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.animateTo(target, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }
    val percent = (target * 100).roundToInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label $percent percent"
        },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(104.dp)) {
            Canvas(modifier = Modifier.size(104.dp)) {
                val stroke = 12.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = colors.track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width = stroke),
                )
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * progress.value, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                )
            }
            Text(
                text = "$percent%",
                style = NitiType.numberMd,
                color = colors.onSurface,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        Text(
            text = label,
            style = NitiType.label,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

// ── Summary list ─────────────────────────────────────────────────────────────

/** One row of the summary card. */
data class SummaryRow(
    val title: String,
    val subtitle: String,
    val value: String,
    val icon: ImageVector,
    val tone: NitiTone,
)

@Composable
fun SummaryCard(rows: List<SummaryRow>, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceLow)
            .padding(vertical = 6.dp),
    ) {
        rows.forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics(mergeDescendants = true) {},
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).background(row.tone.container, CircleShape),
                ) {
                    Icon(row.icon, contentDescription = null, tint = row.tone.onContainer, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = row.title, style = NitiType.bodyStrong, color = colors.onSurface)
                    Text(
                        text = row.subtitle,
                        style = NitiType.caption.copy(fontWeight = FontWeight.Normal),
                        color = colors.onSurfaceVariant,
                    )
                }
                Text(text = row.value, style = NitiType.numberSm, color = colors.onSurface)
            }
        }
    }
}
