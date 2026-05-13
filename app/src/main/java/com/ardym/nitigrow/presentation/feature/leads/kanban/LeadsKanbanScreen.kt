package com.ardym.nitigrow.presentation.feature.leads.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.leads.LeadsViewModel
import com.ardym.nitigrow.presentation.feature.leads.kanban.components.LeadKanbanColumn
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// LeadsKanbanScreen — horizontal-paging Kanban over the existing LeadsViewModel.
// One stage per page (NEW, CONTACTED, QUALIFIED, PROPOSAL, WON, LOST).
//
// Why a HorizontalPager and not horizontal LazyRow (as the existing
// LeadsScreen): per phase-3-mobile.md Section 1.3 the Kanban is the primary
// view for the Leads feature; pagination gives one-stage focus + native
// fling/snap UX. The list-style LazyRow remains the alternate "List" view
// (toggle is a visual stub here — wiring lives in the nav graph).
// ─────────────────────────────────────────────────────────────────────────────

private val STAGES = LeadStage.entries.toList()
private val IndicatorActiveSize = 8.dp
private val IndicatorIdleSize = 6.dp
private val IndicatorSpacing = 8.dp
private val IndicatorRowVPad = 8.dp
private val PageHPad = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsKanbanScreen(
    onLeadClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LeadsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { STAGES.size })
    val scope = rememberCoroutineScope()
    var kanbanMode by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Leads — Kanban",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // Visual toggle only — actually swapping to the list screen
                    // is the navigator's job. See phase-3 doc Section 1.3.
                    IconButton(onClick = { kanbanMode = !kanbanMode }) {
                        Icon(
                            imageVector = if (kanbanMode) {
                                Icons.AutoMirrored.Filled.ViewList
                            } else {
                                Icons.Filled.ViewModule
                            },
                            contentDescription = if (kanbanMode) {
                                "Switch to list view"
                            } else {
                                "Switch to Kanban view"
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Theme.colors.brand,
                    titleContentColor = Theme.colors.paper,
                    navigationIconContentColor = Theme.colors.paper,
                    actionIconContentColor = Theme.colors.paper,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Theme.colors.paper),
        ) {
            state.error?.let {
                ErrorBanner(message = it, modifier = Modifier.padding(12.dp))
            }

            PageIndicatorRow(
                pageCount = STAGES.size,
                currentPage = pagerState.currentPage,
                onSelect = { idx -> scope.launch { pagerState.animateScrollToPage(idx) } },
            )

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = PageHPad),
                pageSpacing = 8.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription =
                            "Leads Kanban, swipe horizontally to change stage."
                    },
            ) { pageIndex ->
                val stage = STAGES[pageIndex]
                LeadKanbanColumn(
                    stage = stage,
                    leads = state.grouped[stage].orEmpty(),
                    onMove = viewModel::onMove,
                    onClick = onLeadClick,
                )
            }
        }
    }
}

@Composable
private fun PageIndicatorRow(
    pageCount: Int,
    currentPage: Int,
    onSelect: (Int) -> Unit,
) {
    val activeColor = Theme.colors.brand
    val idleColor = Theme.colors.muted2

    Row(
        horizontalArrangement = Arrangement.spacedBy(IndicatorSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = IndicatorRowVPad)
            .semantics {
                contentDescription =
                    "Stage ${currentPage + 1} of $pageCount"
            },
    ) {
        repeat(pageCount) { idx ->
            val active = idx == currentPage
            IndicatorDot(
                active = active,
                color = if (active) activeColor else idleColor,
                onClick = { onSelect(idx) },
                stageLabel = STAGES[idx].label,
            )
        }
    }
}

@Composable
private fun IndicatorDot(
    active: Boolean,
    color: Color,
    onClick: () -> Unit,
    stageLabel: String,
) {
    val dotSize = if (active) IndicatorActiveSize else IndicatorIdleSize
    // The dot itself stays small, but we wrap it in an IconButton with a 48dp
    // touch target so it remains tappable per a11y guidelines (Section 5.3).
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = "Go to $stageLabel stage" },
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color),
        )
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "PageIndicator")
@Composable
private fun PageIndicatorPreview() {
    NitiGrowTheme {
        Box(modifier = Modifier.background(Theme.colors.paper).padding(8.dp)) {
            PageIndicatorRow(pageCount = STAGES.size, currentPage = 2, onSelect = {})
        }
    }
}
