package com.websbaba.nitigrow.presentation.feature.leads.kanban.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInr
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInrCompact
import com.websbaba.nitigrow.presentation.feature.leads.components.stageDot
import com.websbaba.nitigrow.core.ui.theme.Bricolage
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// One pipeline column: dot + stage name + count chip + ₹ total, over lead cards.
// Cards re-stage from a long-press menu — the touch equivalent of dragging a
// card between columns — or from the stepper on the lead detail screen.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KanbanColumn(
    stage: LeadStage,
    leads: List<Lead>,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Niti.colors
    val sum = formatInrCompact(leads.sumOf { it.valueInr })
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(colors.surfaceLow)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${stage.label} stage, ${leads.size} leads, $sum total"
                }
        ) {
            Box(Modifier.size(10.dp).background(stageDot(stage, colors), CircleShape))
            Text(text = stage.label, style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface)
            Text(
                text = leads.size.toString(),
                style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            )
            Box(Modifier.weight(1f))
            Text(text = sum, style = NitiType.caption.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
        }
        if (leads.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)) {
                Text(text = "No leads in ${stage.label}", style = NitiType.label.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = leads, key = { it.id }) { lead ->
                    KanbanCard(lead = lead, currentStage = stage, onClick = onClick, onMove = onMove)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KanbanCard(
    lead: Lead,
    currentStage: LeadStage,
    onClick: (String) -> Unit,
    onMove: (leadId: String, to: LeadStage) -> Unit,
) {
    val colors = Niti.colors
    val shape = RoundedCornerShape(18.dp)
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, shape)
                .combinedClickable(
                    onClick = { onClick(lead.id) },
                    onClickLabel = "Open lead",
                    onLongClick = { menuOpen = true },
                    onLongClickLabel = "Move ${lead.contactName} to another stage",
                )
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Avatar(name = lead.contactName, url = null, sizeDp = 32)
                Text(
                    text = lead.contactName,
                    style = NitiType.bodyCompact.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = lead.source,
                    style = NitiType.caption.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Text(
                    text = formatInr(lead.valueInr),
                    style = NitiType.numberSm.copy(fontFamily = Bricolage, fontSize = 15.sp, lineHeight = 20.sp),
                    color = colors.onSurface,
                    maxLines = 1
                )
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            LeadStage.entries.filter { it != currentStage }.forEach { target ->
                DropdownMenuItem(
                    text = { Text("Move to ${target.label}") },
                    onClick = {
                        menuOpen = false
                        onMove(lead.id, target)
                    }
                )
            }
        }
    }
}
