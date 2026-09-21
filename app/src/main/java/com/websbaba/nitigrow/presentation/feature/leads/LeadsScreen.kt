package com.websbaba.nitigrow.presentation.feature.leads

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiSearchField
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.leads.components.LeadStagePill
import com.websbaba.nitigrow.presentation.feature.leads.components.formatInr
import com.websbaba.nitigrow.presentation.feature.leads.components.leadMeta
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// LeadsScreen — the leads list.
//   ◀ Leads · "148 leads · 61 open"          [board] [search]
//   All · New 12 · Contacted · Qualified · Proposal · Won · Lost
//   lead cards: avatar, name, ₹ value, "source · owner · 2h ago", stage pill
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    onBack: () -> Unit = {},
    onLeadClick: (String) -> Unit = {},
    onOpenBoard: () -> Unit = {},
    viewModel: LeadsViewModel = hiltViewModel()
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
                Text(text = "Leads", style = NitiType.title, color = colors.onSurface)
                Text(
                    text = leadsSubtitle(state.leads.size, state.openCount),
                    style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant
                )
            }
            NitiIconButton(icon = NitiIcons.Leads, contentDescription = "Switch to board view", onClick = onOpenBoard)
            NitiIconButton(
                icon = if (state.isSearching) NitiIcons.Close else NitiIcons.Search,
                contentDescription = if (state.isSearching) "Close search" else "Search leads",
                onClick = viewModel::onSearchToggle
            )
        }
        if (state.isSearching) {
            NitiSearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = "Search name, phone or source",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }
        if (state.leads.isNotEmpty()) {
            StageChips(state = state, onSelect = viewModel::onStageFilter)
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
            val visible = state.visibleLeads
            when {
                state.leads.isEmpty() && state.isRefreshing -> LeadsSkeleton()
                state.leads.isEmpty() && state.error != null -> NitiStateView(
                    icon = NitiIcons.Warning,
                    tone = colors.tertiaryTone,
                    title = "Couldn't load leads",
                    body = state.error.orEmpty(),
                    actionLabel = "Retry",
                    onAction = viewModel::refresh
                )
                state.leads.isEmpty() -> NitiStateView(
                    icon = NitiIcons.Leads,
                    tone = colors.tertiaryTone,
                    title = "No leads yet",
                    body = "Leads appear here when a customer shows interest in your business."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    state.error?.let { msg ->
                        item {
                            Text(
                                text = msg,
                                style = NitiType.label,
                                color = colors.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.error.copy(alpha = 0.12f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                text = if (state.query.isNotBlank()) "No leads match \"${state.query.trim()}\"."
                                else "No leads in ${state.stageFilter?.label ?: "this stage"}.",
                                style = NitiType.body,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                            )
                        }
                    }
                    items(items = visible, key = { it.id }) { lead ->
                        LeadListCard(lead = lead, onClick = { onLeadClick(lead.id) })
                    }
                }
            }
        }
    }
}

internal fun leadsSubtitle(total: Int, open: Int): String =
    "$total ${if (total == 1) "lead" else "leads"} · $open open"

@Composable
private fun StageChips(state: LeadsUiState, onSelect: (LeadStage?) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        NitiChip("All", state.stageFilter == null, { onSelect(null) })
        LeadStage.entries.forEach { stage ->
            NitiChip(
                label = stage.label,
                selected = state.stageFilter == stage,
                onClick = { onSelect(stage) },
                count = state.countIn(stage).takeIf { it > 0 }
            )
        }
    }
}

@Composable
private fun LeadListCard(lead: Lead, onClick: () -> Unit) {
    val colors = Niti.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceLow)
            .clickable(role = Role.Button, onClickLabel = "Open lead", onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Avatar(name = lead.contactName, url = null, sizeDp = 48)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = lead.contactName,
                    style = NitiType.titleUi,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatInr(lead.valueInr),
                    style = NitiType.numberSm.copy(fontSize = 17.sp),
                    color = colors.onSurface,
                    maxLines = 1
                )
            }
            Text(
                text = leadMeta(lead.source, lead.ownerName, lead.updatedAt),
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LeadStagePill(lead.stage)
        }
    }
}

@Composable
private fun LeadsSkeleton() {
    val colors = Niti.colors
    val pulse by rememberInfiniteTransition(label = "leadsSkeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "leadsSkeletonAlpha"
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 8.dp).alpha(pulse)
    ) {
        repeat(4) {
            Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(24.dp)).background(colors.surfaceLow))
        }
    }
}
