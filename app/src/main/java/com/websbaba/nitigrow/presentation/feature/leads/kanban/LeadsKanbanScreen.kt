package com.websbaba.nitigrow.presentation.feature.leads.kanban

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.leads.LeadsViewModel
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInrCompact
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.LeadCard
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.LeadKanbanColumn
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.StageHeader
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.stageStyle
import com.websbaba.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// LeadsKanbanScreen — pipeline board over the existing LeadsViewModel.
// Design: back arrow + "Leads pipeline" (Fraunces 21sp) + muted subtitle, then
// a horizontally scrollable row of fixed-width 240dp stage columns (paper2,
// 16dp radius; WON column brandSoft). Cards re-stage via long-press menu —
// the touch equivalent of the web prototype's drag-and-drop — or via the
// stage chips on the lead-detail screen.
// The list/kanban toggle is kept: list mode renders the same leads as a
// single vertical list grouped by stage.
// ─────────────────────────────────────────────────────────────────────────────

private val STAGES = LeadStage.entries.toList()
private val ColumnWidth = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsKanbanScreen(
    onLeadClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LeadsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var kanbanMode by remember { mutableStateOf(true) }

    Scaffold(containerColor = Theme.colors.paper) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PipelineHeader(
                openCount = state.leads.count {
                    it.stage != LeadStage.WON && it.stage != LeadStage.LOST
                },
                kanbanMode = kanbanMode,
                onBack = onBack,
                onToggleMode = { kanbanMode = !kanbanMode },
            )

            state.error?.let {
                ErrorBanner(message = it, modifier = Modifier.padding(horizontal = 16.dp))
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (kanbanMode) {
                    KanbanBoard(
                        grouped = state.grouped,
                        onMove = viewModel::onMove,
                        onLeadClick = onLeadClick,
                    )
                } else {
                    GroupedLeadList(
                        grouped = state.grouped,
                        onMove = viewModel::onMove,
                        onLeadClick = onLeadClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineHeader(
    openCount: Int,
    kanbanMode: Boolean,
    onBack: () -> Unit,
    onToggleMode: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Theme.colors.ink,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Leads pipeline",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                color = Theme.colors.ink,
            )
            Text(
                text = "$openCount open leads · hold a card to move stages",
                fontSize = 12.sp,
                color = Theme.colors.muted,
            )
        }
        IconButton(onClick = onToggleMode) {
            Icon(
                imageVector = if (kanbanMode) {
                    Icons.AutoMirrored.Filled.ViewList
                } else {
                    Icons.Filled.ViewModule
                },
                contentDescription = if (kanbanMode) {
                    "Switch to list view"
                } else {
                    "Switch to board view"
                },
                tint = Theme.colors.ink3,
            )
        }
    }
}

@Composable
private fun KanbanBoard(
    grouped: Map<LeadStage, List<Lead>>,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    onLeadClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Leads pipeline board, scroll horizontally to see stages."
            },
    ) {
        items(items = STAGES, key = { it.name }) { stage ->
            LeadKanbanColumn(
                stage = stage,
                leads = grouped[stage].orEmpty(),
                onMove = onMove,
                onClick = onLeadClick,
                modifier = Modifier
                    .width(ColumnWidth)
                    .fillParentMaxHeight(),
            )
        }
    }
}

/** List mode — same leads, one vertical column, grouped under stage headers. */
@Composable
private fun GroupedLeadList(
    grouped: Map<LeadStage, List<Lead>>,
    onMove: (leadId: String, to: LeadStage) -> Unit,
    onLeadClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        STAGES.forEach { stage ->
            val leads = grouped[stage].orEmpty()
            if (leads.isEmpty()) return@forEach

            item(key = "header-${stage.name}") {
                StageHeader(
                    stage = stage,
                    count = leads.size,
                    sum = formatInrCompact(leads.sumOf { it.valueInr }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                )
            }
            items(items = leads, key = { it.id }) { lead ->
                val style = stageStyle(stage, Theme.colors)
                LeadCard(
                    lead = lead,
                    currentStage = stage,
                    onClick = onLeadClick,
                    onMove = onMove,
                    borderColor = style.cardBorder,
                )
            }
        }
    }
}
