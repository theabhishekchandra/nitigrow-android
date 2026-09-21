package com.websbaba.nitigrow.presentation.feature.inbox.list

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiSearchField
import com.websbaba.nitigrow.presentation.components.NitiSectionLabel
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.ConversationRow
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.InboxFilterSheet
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onConversationClick: (String) -> Unit,
    onStartBroadcast: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    val visibleItems = remember(state.items, state.filter) { state.visibleItems() }
    val expiringCount = remember(state.items) { state.expiringCount() }
    val todayCount = remember(state.items) { state.todayCount() }

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        InboxHeader(
            conversationCount = state.items.size,
            todayCount = todayCount,
            onOpenFilters = { viewModel.onFilterSheetVisibleChange(true) }
        )
        NitiSearchField(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            placeholder = "Search chats and contacts",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        if (state.items.isNotEmpty() || state.filter != InboxFilter.ALL) {
            FilterChipsRow(
                selected = state.filter,
                unreadCount = state.unreadCount,
                expiringCount = expiringCount,
                onSelect = viewModel::onFilterChange
            )
        }
        if (state.query.isNotBlank() && state.items.isNotEmpty()) {
            NitiSectionLabel(text = "Results", modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
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
            when {
                state.isInitialLoading -> InboxSkeleton()
                state.isEmpty && state.error != null -> NitiStateView(
                    icon = NitiIcons.Warning,
                    tone = colors.tertiaryTone,
                    title = "Couldn't load your inbox",
                    body = "Check your connection and try again. Cached chats stay available offline.",
                    actionLabel = "Retry",
                    onAction = viewModel::refresh
                )
                state.isEmpty && state.query.isNotBlank() -> NitiStateView(
                    icon = NitiIcons.Search,
                    tone = colors.secondaryTone,
                    title = "No matches",
                    body = "Nothing found for \"${state.query.trim()}\". Try a name, phone number or message."
                )
                state.isEmpty -> NitiStateView(
                    icon = NitiIcons.Chat,
                    tone = colors.primaryTone,
                    title = "No conversations yet",
                    body = "When customers message your WhatsApp number, their chats appear here.",
                    actionLabel = "Start a broadcast",
                    onAction = onStartBroadcast
                )
                else -> ConversationList(
                    items = visibleItems,
                    query = state.query,
                    error = state.error,
                    onClick = onConversationClick,
                    onLongClick = { c -> viewModel.onTogglePin(c.id, c.isPinned) }
                )
            }
        }
    }

    if (state.isFilterSheetVisible) {
        InboxFilterSheet(
            selectedAssignee = state.assigneeFilter,
            onAssigneeChange = viewModel::onAssigneeFilterChange,
            selectedLabel = state.labelFilter,
            onLabelChange = viewModel::onLabelFilterChange,
            selectedStatus = state.filter,
            onStatusChange = viewModel::onFilterChange,
            onReset = viewModel::onResetFilters,
            onApply = { viewModel.onFilterSheetVisibleChange(false) },
            onDismiss = { viewModel.onFilterSheetVisibleChange(false) }
        )
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun InboxHeader(conversationCount: Int, todayCount: Int, onOpenFilters: () -> Unit) {
    val colors = Niti.colors
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        NitiIconButton(icon = NitiIcons.Filter, contentDescription = "Filter inbox", onClick = onOpenFilters)
    }
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)) {
        Text(
            text = "Inbox",
            style = NitiType.display.copy(letterSpacing = (-0.8).sp),
            color = colors.onSurface
        )
        Text(
            text = when {
                conversationCount == 0 -> "0 conversations"
                todayCount > 0 -> "$todayCount ${if (todayCount == 1) "conversation" else "conversations"} today"
                else -> "$conversationCount ${if (conversationCount == 1) "conversation" else "conversations"}"
            },
            style = NitiType.bodyCompact,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ── Filter chips ─────────────────────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
    selected: InboxFilter,
    unreadCount: Int,
    expiringCount: Int,
    onSelect: (InboxFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        NitiChip("All", selected == InboxFilter.ALL, { onSelect(InboxFilter.ALL) })
        NitiChip("Unread", selected == InboxFilter.UNREAD, { onSelect(InboxFilter.UNREAD) },
            count = unreadCount.takeIf { it > 0 })
        NitiChip("Expiring", selected == InboxFilter.EXPIRING, { onSelect(InboxFilter.EXPIRING) },
            icon = NitiIcons.Clock, count = expiringCount.takeIf { it > 0 })
        NitiChip("Pinned", selected == InboxFilter.PINNED, { onSelect(InboxFilter.PINNED) },
            icon = NitiIcons.Pin)
    }
}

// ── Conversation list ────────────────────────────────────────────────────────

@Composable
private fun ConversationList(
    items: List<Conversation>,
    query: String,
    error: String?,
    onClick: (String) -> Unit,
    onLongClick: (Conversation) -> Unit
) {
    val colors = Niti.colors
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
        error?.let {
            item {
                Text(
                    text = it,
                    style = NitiType.label,
                    color = colors.error,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.error.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    text = "No conversations match this filter.",
                    style = NitiType.body,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp)
                )
            }
        }
        items(items = items, key = { it.id }) { conv ->
            ConversationRow(
                conversation = conv,
                query = query,
                onClick = { onClick(conv.id) },
                onLongClick = { onLongClick(conv) }
            )
        }
    }
}

// ── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun InboxSkeleton() {
    val colors = Niti.colors
    val pulse by rememberInfiniteTransition(label = "skeleton")
        .animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skeletonAlpha"
        )
    // Width fractions per row so the skeleton reads like real conversations.
    val rows = listOf(0.45f to 0.75f, 0.55f to 0.65f, 0.40f to 0.80f, 0.60f to 0.55f, 0.50f to 0.70f)

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        rows.forEach { (nameWidth, previewWidth) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.alpha(pulse)
            ) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(colors.surfaceContainer))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth(nameWidth)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(colors.surfaceContainer)
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth(previewWidth)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceLow)
                    )
                }
            }
        }
    }
}
