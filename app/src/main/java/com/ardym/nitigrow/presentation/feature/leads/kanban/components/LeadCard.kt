package com.ardym.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.presentation.feature.leads.components.LeadAvatar
import com.ardym.nitigrow.presentation.feature.leads.components.formatInr
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// LeadCard (kanban variant) — used inside LeadKanbanColumn.
// Design: card surface, 1dp border (brand-tinted in the WON column), 13dp
// radius, 12dp padding. 28dp initials avatar + name row, secondary line,
// bottom row with ₹ value (brand) and a source mini-pill.
//
// Stage moves: tap opens the lead detail; long-press opens a "Move to …" menu
// (the touch-friendly equivalent of the web prototype's drag-between-columns).
// ─────────────────────────────────────────────────────────────────────────────

private val CardShape = RoundedCornerShape(13.dp)
private val CardPadding = 12.dp
private val PillShape = RoundedCornerShape(999.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeadCard(
    lead: Lead,
    currentStage: LeadStage,
    onClick: (String) -> Unit,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Theme.colors.border,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(Theme.colors.card)
                .border(width = 1.dp, color = borderColor, shape = CardShape)
                .combinedClickable(
                    onClick = { onClick(lead.id) },
                    onLongClick = { menuOpen = true },
                    onLongClickLabel = "Move ${lead.contactName} to another stage",
                    onClickLabel = "Open lead",
                )
                .padding(CardPadding),
        ) {
            // ── Avatar + name row ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LeadAvatar(name = lead.contactName, size = 28.dp, fontSize = 11.sp)
                Text(
                    text = lead.contactName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Secondary line (phone — the domain model has no "need") ────
            Text(
                text = lead.contactPhone,
                fontSize = 12.sp,
                color = Theme.colors.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 7.dp),
            )

            // ── Value + source pill ────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(
                    text = formatInr(lead.valueInr),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Theme.colors.brand,
                )
                Spacer(modifier = Modifier.weight(1f))
                SourcePill(source = lead.source)
            }
        }

        MoveStageMenu(
            expanded = menuOpen,
            currentStage = currentStage,
            onDismiss = { menuOpen = false },
            onSelect = { target ->
                menuOpen = false
                onMove(lead.id, target)
            },
        )
    }
}

@Composable
private fun MoveStageMenu(
    expanded: Boolean,
    currentStage: LeadStage,
    onDismiss: () -> Unit,
    onSelect: (LeadStage) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        LeadStage.entries
            .filter { it != currentStage }
            .forEach { target ->
                DropdownMenuItem(
                    text = { Text("Move to ${target.label}") },
                    onClick = { onSelect(target) },
                )
            }
    }
}

@Composable
private fun SourcePill(source: String) {
    Text(
        text = source,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Theme.colors.muted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(PillShape)
            .background(Theme.colors.paper2)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/**
 * Human-readable relative time. Pure-Kotlin, locale-neutral wording so it
 * matches the web/admin shorthand ("3d ago", "2h ago", "just now").
 * Used by the lead-detail activity timeline.
 */
internal fun relativeTime(then: Instant, now: Instant = Instant.now()): String {
    val seconds = ChronoUnit.SECONDS.between(then, now).coerceAtLeast(0)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 45  -> "just now"
        minutes < 60  -> "${minutes}m ago"
        hours   < 24  -> "${hours}h ago"
        days    < 30  -> "${days}d ago"
        else          -> "${days / 30}mo ago"
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "LeadCard (kanban)")
@Composable
private fun LeadCardPreview() {
    NitiGrowTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LeadCard(
                lead = Lead(
                    id = "ld-1",
                    contactId = "ct-1",
                    contactName = "Vikram Singh",
                    contactPhone = "+91 98765 43210",
                    source = "Campaign reply",
                    stage = LeadStage.NEW,
                    valueInr = 85_000,
                    ownerName = "Anita",
                    notes = null,
                    createdAt = Instant.now().minus(7, ChronoUnit.DAYS),
                    updatedAt = Instant.now().minus(3, ChronoUnit.DAYS),
                ),
                currentStage = LeadStage.NEW,
                onClick = {},
                onMove = { _, _ -> },
            )
        }
    }
}
