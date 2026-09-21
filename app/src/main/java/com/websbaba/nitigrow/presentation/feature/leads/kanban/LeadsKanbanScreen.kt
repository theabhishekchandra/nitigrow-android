package com.websbaba.nitigrow.presentation.feature.leads.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.feature.leads.LeadsViewModel
import com.websbaba.nitigrow.presentation.feature.leads.kanban.components.KanbanColumn
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// LeadsKanbanScreen — pipeline board over the shared LeadsViewModel.
//   ◀ Leads pipeline · "61 open leads · hold a card to move stages"   [list]
//   hint banner, then 300dp stage columns in a horizontal scroller.
// The list view is its own screen (LeadsScreen); the header icon switches to it.
// ─────────────────────────────────────────────────────────────────────────────

private val STAGES = LeadStage.entries.toList()
private val ColumnWidth = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsKanbanScreen(
    onLeadClick: (String) -> Unit,
    onBack: () -> Unit,
    onOpenList: () -> Unit = {},
    viewModel: LeadsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(modifier = Modifier.fillMaxSize().background(colors.surface).statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            NitiIconButton(icon = NitiIcons.Back, contentDescription = "Back", onClick = onBack)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Leads pipeline", style = NitiType.title, color = colors.onSurface)
                Text(
                    text = "${state.openCount} open leads · hold a card to move stages",
                    style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant
                )
            }
            NitiIconButton(icon = NitiIcons.ListBullets, contentDescription = "Switch to list view", onClick = onOpenList)
        }

        val tone = colors.secondaryTone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(tone.container)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(NitiIcons.Cursor, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(18.dp))
            Text(
                text = "Hold a card, then pick a stage to move it.",
                style = NitiType.label,
                color = tone.onContainer
            )
        }

        state.error?.let { msg ->
            Text(
                text = msg,
                style = NitiType.label,
                color = colors.error,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.error.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        val ptrState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            state = ptrState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = ptrState,
                    isRefreshing = state.isRefreshing,
                    containerColor = colors.primaryTone.container,
                    color = colors.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.leads.isEmpty() && !state.isRefreshing) {
                NitiStateView(
                    icon = NitiIcons.Leads,
                    tone = colors.tertiaryTone,
                    title = "No leads yet",
                    body = "Leads appear here when a customer shows interest in your business."
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "Leads pipeline board, scroll horizontally to see stages." }
                ) {
                    items(items = STAGES, key = { it.name }) { stage ->
                        KanbanColumn(
                            stage = stage,
                            leads = state.grouped[stage].orEmpty(),
                            onMove = viewModel::onMove,
                            onClick = onLeadClick,
                            modifier = Modifier.width(ColumnWidth).fillParentMaxHeight()
                        )
                    }
                }
            }
        }
    }
}
