package com.websbaba.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.ui.theme.NitiGrowColors
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// StageHeader — Kanban column header: 8dp colour dot, ALL-CAPS stage name,
// lead count, and the right-aligned ₹ pipeline sum for the column.
//
// Accessibility: announces itself as one logical group via semantics
// (e.g., "Qualified stage, 7 leads, ₹1.4L total") so TalkBack reads it in a
// single chunk rather than label and numbers separately.
// ─────────────────────────────────────────────────────────────────────────────

private val DotSize = 8.dp
private val GroupSpacing = 7.dp

@Composable
fun StageHeader(
    stage: LeadStage,
    count: Int,
    sum: String,
    modifier: Modifier = Modifier,
) {
    val style = stageStyle(stage, Theme.colors)
    val a11yText = "${stage.label} stage, $count leads, $sum total"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GroupSpacing),
        modifier = modifier
            .padding(horizontal = 2.dp)
            .semantics(mergeDescendants = true) { contentDescription = a11yText },
    ) {
        Box(
            modifier = Modifier
                .size(DotSize)
                .clip(CircleShape)
                .background(style.dot),
        )
        Text(
            text = stage.label.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = style.title,
        )
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = style.meta,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = sum,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = style.meta,
        )
    }
}

/**
 * Per-stage kanban styling: header dot, column surface, card border and
 * header text colours. The design specifies the four core stages
 * (NEW = info, CONTACTED = turmeric, QUALIFIED = accent, WON = brand with a
 * brandSoft column); PROPOSAL and LOST exist only in the domain model, so they
 * extend the same language with brandHover (almost-won green) and muted.
 */
internal data class StageStyle(
    val dot: Color,
    val columnBg: Color,
    val cardBorder: Color,
    val title: Color,
    val meta: Color,
)

internal fun stageStyle(stage: LeadStage, c: NitiGrowColors): StageStyle = when (stage) {
    LeadStage.NEW       -> StageStyle(dot = c.info,       columnBg = c.paper2,    cardBorder = c.border,                   title = c.ink,      meta = c.muted)
    LeadStage.CONTACTED -> StageStyle(dot = c.turmeric,   columnBg = c.paper2,    cardBorder = c.border,                   title = c.ink,      meta = c.muted)
    LeadStage.QUALIFIED -> StageStyle(dot = c.accent,     columnBg = c.paper2,    cardBorder = c.border,                   title = c.ink,      meta = c.muted)
    LeadStage.PROPOSAL  -> StageStyle(dot = c.brandHover, columnBg = c.paper2,    cardBorder = c.border,                   title = c.ink,      meta = c.muted)
    LeadStage.WON       -> StageStyle(dot = c.brand,      columnBg = c.brandSoft, cardBorder = c.brand.copy(alpha = .25f), title = c.brandInk, meta = c.brand)
    LeadStage.LOST      -> StageStyle(dot = c.muted2,     columnBg = c.paper2,    cardBorder = c.border,                   title = c.ink,      meta = c.muted)
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "StageHeader — all stages")
@Composable
private fun StageHeaderPreview() {
    NitiGrowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LeadStage.entries.forEach { stage ->
                StageHeader(stage = stage, count = (stage.ordinal + 1) * 2, sum = "₹1.4L")
            }
        }
    }
}
