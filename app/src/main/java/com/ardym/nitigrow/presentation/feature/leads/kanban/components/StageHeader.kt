package com.ardym.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.ui.theme.NitiGrowColors
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// StageHeader — reusable Kanban column header chip.
// Renders the stage label (uppercase, labelLarge) + a circular count badge.
// Colour-coded per stage per phase-3-mobile.md Section 1.3 "Leads Screen".
//
// Accessibility: header announces itself as one logical group via semantics
// (e.g., "Qualified — 7 leads") so TalkBack reads it in a single chunk rather
// than label and number separately.
// ─────────────────────────────────────────────────────────────────────────────

private val HeaderShape = RoundedCornerShape(12.dp)
private val BadgeSize = 26.dp
private val HeaderHPad = 12.dp
private val HeaderVPad = 8.dp
private val GroupSpacing = 8.dp

@Composable
fun StageHeader(
    stage: LeadStage,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val palette = stageColor(stage, Theme.colors)
    val a11yText = "${stage.label} stage, $count leads"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GroupSpacing),
        modifier = modifier
            .clip(HeaderShape)
            .background(palette.surface)
            .padding(horizontal = HeaderHPad, vertical = HeaderVPad)
            .semantics(mergeDescendants = true) { contentDescription = a11yText },
    ) {
        Text(
            text = stage.label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = palette.ink,
            fontWeight = FontWeight.SemiBold,
        )
        CountBadge(count = count, background = palette.ink, foreground = palette.surface)
    }
}

@Composable
private fun CountBadge(count: Int, background: Color, foreground: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = BadgeSize, minHeight = BadgeSize)
            .size(BadgeSize)
            .clip(CircleShape)
            .background(background),
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = foreground,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Resolves the colour pair for a [LeadStage]. Centralised so [LeadKanbanColumn]
 * and the kanban LeadCard use the same palette as the header.
 *
 * Per phase-3 spec:
 *   NEW = info, CONTACTED = brand, QUALIFIED = success,
 *   PROPOSAL = warning, WON = success, LOST = muted.
 */
internal data class StagePalette(val surface: Color, val ink: Color)

@Composable
internal fun stageColor(stage: LeadStage, c: NitiGrowColors): StagePalette = when (stage) {
    LeadStage.NEW       -> StagePalette(surface = c.brandSoft,    ink = c.info)
    LeadStage.CONTACTED -> StagePalette(surface = c.brandSoft,    ink = c.brand)
    LeadStage.QUALIFIED -> StagePalette(surface = c.brandSoft,    ink = c.success)
    LeadStage.PROPOSAL  -> StagePalette(surface = c.turmericSoft, ink = c.warning)
    LeadStage.WON       -> StagePalette(surface = c.brandSoft,    ink = c.success)
    LeadStage.LOST      -> StagePalette(surface = c.paper3,       ink = c.muted)
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "StageHeader — all stages")
@Composable
private fun StageHeaderPreview() {
    NitiGrowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LeadStage.entries.forEach { stage ->
                StageHeader(stage = stage, count = (stage.ordinal + 1) * 3)
            }
        }
    }
}
