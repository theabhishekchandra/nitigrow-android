package com.ardym.nitigrow.presentation.feature.inbox.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.inbox.list.components.ConversationRow
import com.ardym.nitigrow.presentation.feature.inbox.list.components.InboxSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onConversationClick: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inbox", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            InboxSearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange
            )
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isInitialLoading -> CenteredLoading()
                    state.isEmpty -> EmptyView(query = state.query, error = state.error)
                    else -> ConversationList(
                        items = state.items,
                        error = state.error,
                        onClick = onConversationClick,
                        onLongClick = { c -> viewModel.onTogglePin(c.id, c.isPinned) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyView(query: String, error: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error != null) {
            ErrorBanner(message = error)
            Spacer(Modifier.height(12.dp))
            Text("Pull down to retry", style = MaterialTheme.typography.bodySmall)
        } else if (query.isNotBlank()) {
            Text("No matches for \"$query\"", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("No conversations yet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a campaign or wait for inbound messages.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConversationList(
    items: List<com.ardym.nitigrow.domain.model.Conversation>,
    error: String?,
    onClick: (String) -> Unit,
    onLongClick: (com.ardym.nitigrow.domain.model.Conversation) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        error?.let {
            item { ErrorBanner(message = it, modifier = Modifier.padding(16.dp)) }
        }
        items(items = items, key = { it.id }) { conv ->
            ConversationRow(
                conversation = conv,
                onClick = { onClick(conv.id) },
                onLongClick = { onLongClick(conv) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 80.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
