package com.websbaba.nitigrow.presentation.feature.templates.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.presentation.feature.templates.Template
import com.websbaba.nitigrow.presentation.feature.templates.TemplateCategory
import com.websbaba.nitigrow.presentation.feature.templates.TemplateLanguage
import com.websbaba.nitigrow.presentation.feature.templates.TemplateStatus
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// TemplateCard — list row in TemplatesScreen.
//
// Matches the design language: card surface with 15dp radius, monospace
// template name, category mini-pill, right-aligned status pill, and a 2-line
// body preview. A REJECTED template tints the card border danger and shows
// Meta's rejection reason (when present) as the body line in danger color.
// ─────────────────────────────────────────────────────────────────────────────

private val CardRadius: Dp = 15.dp
private val CardPadding: Dp = 14.dp
private val BodyTopGap: Dp = 7.dp
private const val BodyPreviewLines: Int = 2

private val CardShape = RoundedCornerShape(CardRadius)
private val PillShape = RoundedCornerShape(999.dp)

@Composable
fun TemplateCard(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRejected = template.status == TemplateStatus.REJECTED
    val borderColor =
        if (isRejected) Theme.colors.danger.copy(alpha = 0.25f) else Theme.colors.border

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Theme.colors.card)
            .border(1.dp, borderColor, CardShape)
            .clickable(onClick = onClick)
            .padding(CardPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = template.name,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                CategoryMiniPill(category = template.category)
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(status = template.status)
        }

        Spacer(Modifier.height(BodyTopGap))

        // REJECTED templates surface Meta's rejection reason as the body line.
        val rejectionReason = template.rejectionReason
        if (isRejected && !rejectionReason.isNullOrBlank()) {
            Text(
                text = rejectionReason,
                fontSize = 11.5.sp,
                lineHeight = 17.sp,
                color = Theme.colors.danger,
                maxLines = BodyPreviewLines,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = template.body,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Theme.colors.ink3,
                maxLines = BodyPreviewLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CategoryMiniPill(category: TemplateCategory) {
    val label = when (category) {
        TemplateCategory.MARKETING -> "Marketing"
        TemplateCategory.UTILITY -> "Utility"
        TemplateCategory.AUTHENTICATION -> "Auth"
    }
    Pill(
        label = label,
        background = Theme.colors.paper2,
        foreground = Theme.colors.muted,
        horizontalPadding = 8.dp,
        verticalPadding = 2.dp,
    )
}

@Composable
private fun StatusPill(status: TemplateStatus) {
    val (bg, fg, label) = when (status) {
        TemplateStatus.APPROVED ->
            Triple(Theme.colors.brandSoft, Theme.colors.brand, "APPROVED")
        TemplateStatus.PENDING ->
            Triple(Theme.colors.turmericSoft, Theme.colors.turmericInk, "IN REVIEW")
        TemplateStatus.REJECTED ->
            Triple(Theme.colors.danger.copy(alpha = 0.12f), Theme.colors.danger, "REJECTED")
    }
    Pill(
        label = label,
        background = bg,
        foreground = fg,
        horizontalPadding = 9.dp,
        verticalPadding = 3.dp,
    )
}

@Composable
private fun Pill(
    label: String,
    background: Color,
    foreground: Color,
    horizontalPadding: Dp,
    verticalPadding: Dp,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        color = foreground,
        maxLines = 1,
        modifier = Modifier
            .clip(PillShape)
            .background(background)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    )
}

@Preview(showBackground = true)
@Composable
private fun TemplateCardPreview() {
    NitiGrowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            TemplateCard(
                template = Template(
                    id = "t1",
                    name = "mango_festival_offer",
                    language = TemplateLanguage.HI,
                    category = TemplateCategory.MARKETING,
                    status = TemplateStatus.APPROVED,
                    body = "Aam ka season aa gaya! Mango barfi, aamras aur mango kalakand — is week 15% off…",
                    updatedAt = Instant.now(),
                ),
                onClick = {},
            )
            TemplateCard(
                template = Template(
                    id = "t2",
                    name = "payment_reminder",
                    language = TemplateLanguage.EN,
                    category = TemplateCategory.UTILITY,
                    status = TemplateStatus.PENDING,
                    body = "Reminder: aapka payment of ₹{{1}} pending hai. Link: {{2}}",
                    updatedAt = Instant.now(),
                ),
                onClick = {},
            )
            TemplateCard(
                template = Template(
                    id = "t3",
                    name = "catering_quote",
                    language = TemplateLanguage.EN,
                    category = TemplateCategory.MARKETING,
                    status = TemplateStatus.REJECTED,
                    body = "Catering chahiye? Humse quote lo.",
                    rejectionReason = "Meta: promotional content not allowed in Utility category. Edit and resubmit.",
                    updatedAt = Instant.now(),
                ),
                onClick = {},
            )
        }
    }
}
