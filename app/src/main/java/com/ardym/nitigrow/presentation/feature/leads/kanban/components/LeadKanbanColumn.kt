package com.ardym.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.presentation.feature.leads.components.formatInrCompact
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// LeadKanbanColumn — one fixed-width pipeline column in the horizontally
// scrollable kanban board. paper2 surface (brandSoft for WON), 16dp radius,
// 12dp padding; StageHeader (dot + caps name + count + ₹ sum) above the cards.
// Drag is not implemented — cards re-stage via long-press menu (see LeadCard)
// or via the stage chips on the lead-detail screen.
// ─────────────────────────────────────────────────────────────────────────────

private val ColumnShape = RoundedCornerShape(16.dp)
private val ColumnPadding = 12.dp
private val CardGap = 9.dp
private val EmptyVerticalPadding = 48.dp

@Composable
fun LeadKanbanColumn(
    stage: LeadStage,
    leads: List<Lead>,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = stageStyle(stage, Theme.colors)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(ColumnShape)
            .background(style.columnBg)
            .padding(ColumnPadding),
    ) {
        StageHeader(
            stage = stage,
            count = leads.size,
            sum = formatInrCompact(leads.sumOf { it.valueInr }),
            modifier = Modifier.fillMaxWidth(),
        )

        if (leads.isEmpty()) {
            EmptyColumn(stage = stage)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = CardGap),
                verticalArrangement = Arrangement.spacedBy(CardGap),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = leads, key = { it.id }) { lead ->
                    LeadCard(
                        lead = lead,
                        currentStage = stage,
                        onClick = onClick,
                        onMove = onMove,
                        borderColor = style.cardBorder,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyColumn(stage: LeadStage) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = EmptyVerticalPadding),
    ) {
        Text(
            text = "No leads in ${stage.label}",
            fontSize = 12.sp,
            color = Theme.colors.muted,
        )
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "LeadKanbanColumn — populated", widthDp = 264, heightDp = 640)
@Composable
private fun LeadKanbanColumnPreview() {
    NitiGrowTheme {
        LeadKanbanColumn(
            stage = LeadStage.QUALIFIED,
            leads = previewLeads(),
            onMove = { _, _ -> },
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "LeadKanbanColumn — empty WON", widthDp = 264, heightDp = 640)
@Composable
private fun LeadKanbanColumnEmptyPreview() {
    NitiGrowTheme {
        LeadKanbanColumn(
            stage = LeadStage.WON,
            leads = emptyList(),
            onMove = { _, _ -> },
            onClick = {},
        )
    }
}

private fun previewLeads(): List<Lead> {
    val now = Instant.now()
    return listOf(
        Lead(
            id = "1", contactId = "c1", contactName = "Rahul Khanna",
            contactPhone = "+91 98765 43210", source = "Referral",
            stage = LeadStage.QUALIFIED, valueInr = 1_40_000, ownerName = "Anita",
            notes = null,
            createdAt = now.minus(8, ChronoUnit.DAYS),
            updatedAt = now.minus(3, ChronoUnit.DAYS),
        ),
        Lead(
            id = "2", contactId = "c2", contactName = "Nisha Patel",
            contactPhone = "+91 90000 11122", source = "Campaign reply",
            stage = LeadStage.QUALIFIED, valueInr = 18_000, ownerName = "Rohit",
            notes = null,
            createdAt = now.minus(20, ChronoUnit.DAYS),
            updatedAt = now.minus(2, ChronoUnit.HOURS),
        ),
    )
}
