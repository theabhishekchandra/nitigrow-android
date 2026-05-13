package com.ardym.nitigrow.presentation.feature.templates.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.presentation.feature.templates.Template
import com.ardym.nitigrow.presentation.feature.templates.TemplateCategory
import com.ardym.nitigrow.presentation.feature.templates.TemplateStatus
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Duration
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// TemplateCard — list row in TemplatesScreen.
//
// Each row renders the template name + a compact metadata strip (language pill,
// category chip, status chip) and a 3-line body preview styled like a WhatsApp
// inbound bubble (warm-cream background + BubbleInShape) so authors get a
// faithful sense of how their copy will look on a real device.
// ─────────────────────────────────────────────────────────────────────────────

private val CardPadding: Dp = 14.dp
private val SectionGap: Dp = 8.dp
private val BubblePadding: Dp = 12.dp
private val PillCornerRadius: Dp = 6.dp
private val ChipCornerRadius: Dp = 8.dp
private val PillHorizontalPadding: Dp = 8.dp
private val PillVerticalPadding: Dp = 3.dp
private val BodyPreviewLines: Int = 3

@Composable
fun TemplateCard(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
    ) {
        Column(modifier = Modifier.padding(CardPadding)) {
            // Title + status chip on the right
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(template.status)
            }

            Spacer(Modifier.height(SectionGap))

            // Language pill + category chip
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguagePill(label = template.language.label)
                CategoryChip(category = template.category)
            }

            Spacer(Modifier.height(SectionGap))

            // WhatsApp-style body preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Theme.colors.bubbleIn,
                contentColor = Theme.colors.bubbleInInk,
                shape = BubbleInShape,
                border = BorderStroke(1.dp, Theme.colors.bubbleInBorder),
            ) {
                Column(modifier = Modifier.padding(BubblePadding)) {
                    Text(
                        text = "WhatsApp preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = Theme.colors.muted,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = template.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Theme.colors.bubbleInInk,
                        maxLines = BodyPreviewLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Optional rejection reason
            template.rejectionReason?.let { reason ->
                Spacer(Modifier.height(SectionGap))
                Text(
                    text = "Reason: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.colors.danger,
                )
            }

            Spacer(Modifier.height(SectionGap))

            Text(
                text = "Updated ${relativeTime(template.updatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
            )
        }
    }
}

@Composable
private fun LanguagePill(label: String) {
    Surface(
        color = Theme.colors.paper2,
        contentColor = Theme.colors.ink2,
        shape = RoundedCornerShape(PillCornerRadius),
        modifier = Modifier.border(1.dp, Theme.colors.border, RoundedCornerShape(PillCornerRadius)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
        )
    }
}

@Composable
private fun CategoryChip(category: TemplateCategory) {
    val (bg, fg, label) = when (category) {
        TemplateCategory.MARKETING -> Triple(Theme.colors.accentSoft, Theme.colors.accent, "Marketing")
        TemplateCategory.UTILITY -> Triple(Theme.colors.brandSoft, Theme.colors.brand, "Utility")
        TemplateCategory.AUTHENTICATION -> Triple(Theme.colors.turmericSoft, Theme.colors.ink2, "Auth")
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(ChipCornerRadius))
            .background(bg)
            .padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
    )
}

@Composable
fun StatusChip(status: TemplateStatus) {
    val (bg, fg, label) = when (status) {
        TemplateStatus.APPROVED -> Triple(Theme.colors.brandSoft, Theme.colors.success, "Approved")
        TemplateStatus.PENDING -> Triple(Theme.colors.turmericSoft, Theme.colors.warning, "Pending")
        TemplateStatus.REJECTED -> Triple(Theme.colors.accentSoft, Theme.colors.danger, "Rejected")
    }
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(ChipCornerRadius),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
        )
    }
}

/** Lightweight relative-time formatter so we don't pull in DateTimeFormatter just for this card. */
private fun relativeTime(then: Instant): String {
    val secs = Duration.between(then, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        secs < 60 -> "just now"
        secs < 3600 -> "${secs / 60}m ago"
        secs < 86_400 -> "${secs / 3600}h ago"
        secs < 86_400 * 30 -> "${secs / 86_400}d ago"
        else -> "${secs / (86_400 * 30)}mo ago"
    }
}

