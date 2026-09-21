package com.websbaba.nitigrow.presentation.feature.campaigns.list

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiExtendedFab
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiSearchField
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.feature.campaigns.list.components.CampaignRow
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiTone
import com.websbaba.nitigrow.core.ui.theme.NitiType

// ─────────────────────────────────────────────────────────────────────────────
// CampaignsScreen — "Broadcasts" tab root.
//
//   [search]
//   Broadcasts · "5 broadcasts · 1 sending now"
//   [Templates ›] [Analytics ›]
//   All · Sending · Scheduled · Completed · Draft
//   broadcast cards …                              [＋ New broadcast]
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    onCreate: () -> Unit,
    onCampaignClick: (String) -> Unit,
    onTemplates: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    viewModel: CampaignsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                NitiIconButton(
                    icon = if (state.isSearching) NitiIcons.Close else NitiIcons.Search,
                    contentDescription = if (state.isSearching) "Close search" else "Search broadcasts",
                    onClick = viewModel::onSearchToggle
                )
            }
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)) {
                Text(text = "Broadcasts", style = NitiType.display.copy(letterSpacing = (-0.8).sp), color = colors.onSurface)
                Text(
                    text = subtitle(total = state.items.size, sending = state.sendingCount),
                    style = NitiType.bodyCompact,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (state.isSearching) {
                NitiSearchField(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = "Search broadcasts",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
            ShortcutRow(
                approvedTemplates = state.approvedTemplates,
                onTemplates = onTemplates,
                onAnalytics = onAnalytics
            )
            if (state.items.isNotEmpty()) {
                FilterChips(
                    selected = state.filter,
                    sending = state.sendingCount,
                    scheduled = state.scheduledCount,
                    onSelect = viewModel::onFilterChange
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
                val visible = state.visibleItems
                when {
                    state.isInitialLoading -> BroadcastsSkeleton()
                    state.items.isEmpty() && state.error != null -> NitiStateView(
                        icon = NitiIcons.Warning,
                        tone = colors.tertiaryTone,
                        title = "Couldn't load broadcasts",
                        body = state.error.orEmpty(),
                        actionLabel = "Retry",
                        onAction = viewModel::refresh
                    )
                    state.items.isEmpty() -> NitiStateView(
                        icon = NitiIcons.Megaphone,
                        tone = colors.primaryTone,
                        title = "No broadcasts yet",
                        body = "Send an approved template to a group of contacts and track how it performs.",
                        actionLabel = "New broadcast",
                        onAction = onCreate
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    text = if (state.query.isNotBlank()) "No broadcasts match \"${state.query.trim()}\"."
                                    else "No ${state.filter.label.lowercase()} broadcasts.",
                                    style = NitiType.body,
                                    color = colors.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                                )
                            }
                        }
                        items(items = visible, key = { it.id }) { c ->
                            CampaignRow(campaign = c, onClick = { onCampaignClick(c.id) })
                        }
                    }
                }
            }
        }

        NitiExtendedFab(
            text = "New broadcast",
            icon = NitiIcons.Plus,
            onClick = onCreate,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
        )
    }
}

internal fun subtitle(total: Int, sending: Int): String {
    val count = "$total ${if (total == 1) "broadcast" else "broadcasts"}"
    return if (sending > 0) "$count · $sending sending now" else count
}

// ── Shortcuts ────────────────────────────────────────────────────────────────

@Composable
private fun ShortcutRow(approvedTemplates: Int, onTemplates: () -> Unit, onAnalytics: () -> Unit) {
    val colors = Niti.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Shortcut(
            title = "Templates",
            subtitle = if (approvedTemplates > 0) "$approvedTemplates approved" else "Message templates",
            icon = NitiIcons.Document,
            tone = colors.secondaryTone,
            onClick = onTemplates,
            modifier = Modifier.weight(1f)
        )
        Shortcut(
            title = "Analytics",
            subtitle = "Last 30 days",
            icon = NitiIcons.BarChart,
            tone = colors.infoTone,
            onClick = onAnalytics,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Shortcut(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tone: NitiTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(tone.container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold), color = tone.onContainer)
            Text(
                text = subtitle,
                style = NitiType.caption.copy(fontWeight = FontWeight.Normal),
                color = tone.onContainer.copy(alpha = 0.9f)
            )
        }
        Icon(NitiIcons.ChevronRight, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(18.dp))
    }
}

// ── Filter chips ─────────────────────────────────────────────────────────────

@Composable
private fun FilterChips(
    selected: BroadcastFilter,
    sending: Int,
    scheduled: Int,
    onSelect: (BroadcastFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        BroadcastFilter.entries.forEach { f ->
            NitiChip(
                label = f.label,
                selected = selected == f,
                onClick = { onSelect(f) },
                count = when (f) {
                    BroadcastFilter.SENDING -> sending.takeIf { it > 0 }
                    BroadcastFilter.SCHEDULED -> scheduled.takeIf { it > 0 }
                    else -> null
                }
            )
        }
    }
}

// ── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun BroadcastsSkeleton() {
    val colors = Niti.colors
    val pulse by rememberInfiniteTransition(label = "broadcastsSkeleton")
        .animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "broadcastsSkeletonAlpha"
        )
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 8.dp).alpha(pulse)
    ) {
        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceLow)
            )
        }
    }
}
