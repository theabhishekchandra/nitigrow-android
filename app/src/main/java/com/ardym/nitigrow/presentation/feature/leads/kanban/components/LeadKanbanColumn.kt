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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// LeadKanbanColumn — body of a single Kanban "page" inside the HorizontalPager.
// Phase-3 spec: per-stage column with header + LazyColumn of leads. Drag is
// not implemented here — we provide a per-card overflow menu via LeadCard
// for moving to any other stage, and horizontal page-flick for adjacent
// stage navigation.
// ─────────────────────────────────────────────────────────────────────────────

private val ColumnShape = RoundedCornerShape(16.dp)
private val ColumnPadding = 12.dp
private val HeaderBottomGap = 12.dp
private val EmptyVerticalPadding = 48.dp

@Composable
fun LeadKanbanColumn(
    stage: LeadStage,
    leads: List<Lead>,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(ColumnShape)
            .background(Theme.colors.paper2)
            .padding(ColumnPadding),
    ) {
        StageHeader(
            stage = stage,
            count = leads.size,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.padding(top = HeaderBottomGap))

        if (leads.isEmpty()) {
            EmptyColumn(stage = stage)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = leads, key = { it.id }) { lead ->
                    LeadCard(
                        lead = lead,
                        currentStage = stage,
                        onClick = onClick,
                        onMove = onMove,
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
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted,
        )
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "LeadKanbanColumn — populated", widthDp = 360, heightDp = 640)
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

@Preview(showBackground = true, name = "LeadKanbanColumn — empty", widthDp = 360, heightDp = 640)
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
            id = "1", contactId = "c1", contactName = "Priya Sharma",
            contactPhone = "+91 98765 43210", source = "WhatsApp Ad",
            stage = LeadStage.QUALIFIED, valueInr = 48_500, ownerName = "Rahul",
            notes = null,
            createdAt = now.minus(8, ChronoUnit.DAYS),
            updatedAt = now.minus(3, ChronoUnit.DAYS),
        ),
        Lead(
            id = "2", contactId = "c2", contactName = "Anand Mehta",
            contactPhone = "+91 90000 11122", source = "Referral",
            stage = LeadStage.QUALIFIED, valueInr = 1_25_000, ownerName = "Priya",
            notes = null,
            createdAt = now.minus(20, ChronoUnit.DAYS),
            updatedAt = now.minus(2, ChronoUnit.HOURS),
        ),
    )
}
