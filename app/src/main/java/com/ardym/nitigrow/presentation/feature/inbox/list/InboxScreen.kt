package com.ardym.nitigrow.presentation.feature.inbox.list

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.inbox.list.components.ConversationRow
import com.ardym.nitigrow.presentation.feature.inbox.list.components.InboxFilterSheet
import com.ardym.nitigrow.presentation.feature.inbox.list.components.InboxSearchBar
import com.ardym.nitigrow.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onConversationClick: (String) -> Unit,
    onStartBroadcast: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visibleItems = remember(state.items, state.filter) { state.visibleItems() }
    val expiringCount = remember(state.items) { state.expiringCount() }

    Scaffold(containerColor = Theme.colors.paper) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            InboxHeader(onOpenFilters = { viewModel.onFilterSheetVisibleChange(true) })
            InboxSearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange
            )
            FilterChipsRow(
                selected = state.filter,
                unreadCount = state.unreadCount,
                expiringCount = expiringCount,
                onSelect = viewModel::onFilterChange
            )

            val ptrState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                state = ptrState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = ptrState,
                        isRefreshing = state.isRefreshing,
                        containerColor = Theme.colors.brandSoft,
                        color = Theme.colors.brand,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isInitialLoading -> InboxSkeleton()
                    state.isEmpty && state.error != null -> InboxErrorState(
                        onRetry = viewModel::refresh
                    )
                    state.isEmpty && state.query.isNotBlank() -> NoMatchesState(query = state.query)
                    state.isEmpty -> InboxEmptyState(onStartBroadcast = onStartBroadcast)
                    else -> ConversationList(
                        items = visibleItems,
                        error = state.error,
                        onClick = onConversationClick,
                        onLongClick = { c -> viewModel.onTogglePin(c.id, c.isPinned) }
                    )
                }
            }
        }
    }

    if (state.isFilterSheetVisible) {
        InboxFilterSheet(
            selectedAssignee = state.assigneeFilter,
            onAssigneeChange = viewModel::onAssigneeFilterChange,
            selectedLabel = state.labelFilter,
            onLabelChange = viewModel::onLabelFilterChange,
            onApply = { viewModel.onFilterSheetVisibleChange(false) },
            onDismiss = { viewModel.onFilterSheetVisibleChange(false) }
        )
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun InboxHeader(onOpenFilters: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp)
    ) {
        Text(
            text = "Inbox",
            style = MaterialTheme.typography.displaySmall,
            color = Theme.colors.ink,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.card)
                .border(1.dp, Theme.colors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenFilters),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = "Filter inbox",
                tint = Theme.colors.ink2,
                modifier = Modifier.size(18.dp)
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 12.dp)
    ) {
        FilterChip(
            label = "All",
            selected = selected == InboxFilter.ALL,
            onClick = { onSelect(InboxFilter.ALL) }
        )
        FilterChip(
            label = if (unreadCount > 0) "Unread · $unreadCount" else "Unread",
            selected = selected == InboxFilter.UNREAD,
            onClick = { onSelect(InboxFilter.UNREAD) }
        )
        FilterChip(
            label = if (expiringCount > 0) "Expiring · $expiringCount" else "Expiring",
            selected = selected == InboxFilter.EXPIRING,
            onClick = { onSelect(InboxFilter.EXPIRING) }
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Theme.colors.paper else Theme.colors.ink3,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Theme.colors.brand else Theme.colors.paper2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

// ── Conversation list ────────────────────────────────────────────────────────

@Composable
private fun ConversationList(
    items: List<Conversation>,
    error: String?,
    onClick: (String) -> Unit,
    onLongClick: (Conversation) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        error?.let {
            item { ErrorBanner(message = it, modifier = Modifier.padding(16.dp)) }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    text = "No conversations match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Theme.colors.muted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 32.dp)
                )
            }
        }
        items(items = items, key = { it.id }) { conv ->
            ConversationRow(
                conversation = conv,
                onClick = { onClick(conv.id) },
                onLongClick = { onLongClick(conv) }
            )
            HorizontalDivider(thickness = 1.dp, color = Theme.colors.border2)
        }
    }
}

// ── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun InboxSkeleton() {
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 4.dp)
    ) {
        rows.forEach { (nameWidth, previewWidth) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(pulse)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Theme.colors.paper3)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(nameWidth)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Theme.colors.paper3)
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(previewWidth)
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Theme.colors.paper2)
                    )
                }
            }
        }
    }
}

// ── Empty / error states ─────────────────────────────────────────────────────

@Composable
private fun InboxEmptyState(onStartBroadcast: () -> Unit) {
    StateScaffold {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Theme.colors.paper2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ChatBubble,
                contentDescription = null,
                tint = Theme.colors.muted3,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No conversations yet",
            style = MaterialTheme.typography.headlineSmall,
            color = Theme.colors.ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "When customers message your WhatsApp number, their chats appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onStartBroadcast,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Theme.colors.brand,
                contentColor = Theme.colors.paper
            ),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 11.dp)
        ) {
            Text(
                text = "Start a broadcast",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InboxErrorState(onRetry: () -> Unit) {
    StateScaffold {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Theme.colors.danger.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Theme.colors.danger,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Couldn't load your inbox",
            style = MaterialTheme.typography.headlineSmall,
            color = Theme.colors.ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Check your connection and try again. Cached chats stay available offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Retry",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Theme.colors.ink,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.card)
                .border(1.dp, Theme.colors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 26.dp, vertical = 11.dp)
        )
    }
}

@Composable
private fun NoMatchesState(query: String) {
    StateScaffold {
        Text(
            text = "No matches for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = Theme.colors.muted
        )
    }
}

/** Shared centered-column wrapper for the empty / error / no-match states. */
@Composable
private fun StateScaffold(content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp, bottom = 80.dp)
    ) {
        content()
    }
}
