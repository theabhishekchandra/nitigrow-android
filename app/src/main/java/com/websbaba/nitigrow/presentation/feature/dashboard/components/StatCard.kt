package com.websbaba.nitigrow.presentation.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme

/** Tone of the small delta pill on a KPI card. */
enum class StatPillTone {
    /** Positive delta / healthy metric — brandSoft bg, brand text. */
    POSITIVE,

    /** Needs attention — accentSoft bg, accent text. */
    URGENT,
}

/**
 * KPI card — card surface, 1dp warm border, 16dp radius, 14dp padding.
 * ALL-CAPS tracked micro-label over a 26sp bold number, with an optional
 * pill for a delta ("+12%") or urgency ("2 urgent").
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    pillText: String? = null,
    pillTone: StatPillTone = StatPillTone.POSITIVE
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.colors.card)
            .border(1.dp, Theme.colors.border, shape)
            .padding(14.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = Theme.colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Theme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (pillText != null) {
                val (bg, fg) = when (pillTone) {
                    StatPillTone.POSITIVE -> Theme.colors.brandSoft to Theme.colors.brand
                    StatPillTone.URGENT -> Theme.colors.accentSoft to Theme.colors.accent
                }
                Text(
                    text = pillText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(bg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/** Lays a list of KPI cards in one equal-width row (design grid gap = 10dp). */
@Composable
fun StatCardRow(
    cards: List<@Composable (Modifier) -> Unit>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        cards.forEach { it(Modifier.weight(1f)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    NitiGrowTheme {
        Column(Modifier.background(Theme.colors.paper).padding(18.dp)) {
            StatCardRow(
                cards = listOf(
                    { mod -> StatCard("Conversations today", "34", mod, pillText = "+12%") },
                    { mod ->
                        StatCard(
                            "Pending replies", "6", mod,
                            pillText = "2 urgent", pillTone = StatPillTone.URGENT
                        )
                    }
                )
            )
        }
    }
}
