package com.ardym.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// LeadCard (kanban variant) — used inside LeadKanbanColumn.
// Differs from the list-screen LeadCard:
//   • shows source pill + owner + relative "updated" timestamp
//   • exposes a single "Move →" overflow that opens a DropdownMenu of OTHER
//     stages as destination targets. Long-press also opens this menu so users
//     who don't reach for the kebab icon can still re-stage from the body.
//
// Theme tokens used:
//   • Theme.colors.card  — card surface
//   • Theme.colors.brand — value (₹) accent text
//
// Accessibility: the overflow IconButton has a descriptive contentDescription;
// the whole card has a click + long-click label set on combinedClickable.
// ─────────────────────────────────────────────────────────────────────────────

private val inrFormat = NumberFormat.getInstance(Locale("en", "IN"))

private val CardShape = RoundedCornerShape(12.dp)
private val CardPadding = 12.dp
private val RowSpacing = 6.dp
private val PillShape = RoundedCornerShape(999.dp)
private val MoveButtonSize = 40.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeadCard(
    lead: Lead,
    currentStage: LeadStage,
    onClick: (String) -> Unit,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick(lead.id) },
                onLongClick = { menuOpen = true },
                onLongClickLabel = "Move to another stage",
                onClickLabel = "Open lead",
            ),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(CardPadding)) {

            // ── Header row: name + overflow ────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = lead.contactName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(MoveButtonSize),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Move ${lead.contactName} to another stage",
                            tint = Theme.colors.muted,
                        )
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

            // ── Value (₹) — brand accent ───────────────────────────────────
            Text(
                text = "₹${inrFormat.format(lead.valueInr)}",
                style = MaterialTheme.typography.titleMedium,
                color = Theme.colors.brand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = RowSpacing),
            )

            // ── Source pill + owner ────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = RowSpacing),
            ) {
                SourcePill(source = lead.source)
                lead.ownerName?.let { owner ->
                    Text(
                        text = owner,
                        style = MaterialTheme.typography.labelSmall,
                        color = Theme.colors.ink3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // ── Updated-at relative timestamp ──────────────────────────────
            Text(
                text = relativeTime(lead.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
                modifier = Modifier.padding(top = RowSpacing),
            )
        }
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
        style = MaterialTheme.typography.labelSmall,
        color = Theme.colors.brandInk,
        modifier = Modifier
            .clip(PillShape)
            .background(Theme.colors.brandSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * Human-readable relative time. Pure-Kotlin, locale-neutral wording so it
 * matches the web/admin shorthand ("3d ago", "2h ago", "just now").
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
                    contactName = "Priya Sharma",
                    contactPhone = "+91 98765 43210",
                    source = "WhatsApp Ad",
                    stage = LeadStage.QUALIFIED,
                    valueInr = 48_500,
                    ownerName = "Rahul K.",
                    notes = null,
                    createdAt = Instant.now().minus(7, ChronoUnit.DAYS),
                    updatedAt = Instant.now().minus(3, ChronoUnit.DAYS),
                ),
                currentStage = LeadStage.QUALIFIED,
                onClick = {},
                onMove = { _, _ -> },
            )
        }
    }
}
