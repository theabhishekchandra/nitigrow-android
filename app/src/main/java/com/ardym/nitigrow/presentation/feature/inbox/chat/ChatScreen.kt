package com.ardym.nitigrow.presentation.feature.inbox.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ardym.nitigrow.core.util.TimeFormatter
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.presentation.feature.inbox.chat.components.DateSeparator
import com.ardym.nitigrow.presentation.feature.inbox.chat.components.MessageBubble
import com.ardym.nitigrow.presentation.feature.inbox.chat.components.MessageInput
import com.ardym.nitigrow.presentation.feature.inbox.chat.components.TypingIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = viewModel.messages.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { ev ->
            when (ev) {
                is ChatEffect.ShowError -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    LaunchedEffect(items.itemCount) {
        if (items.itemCount > 0 && listState.firstVisibleItemIndex < 3) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (state.typing) TypingIndicator(name = "Contact")
                MessageInput(
                    text = state.draft,
                    onTextChange = viewModel::onDraftChange,
                    onSend = {
                        viewModel.onSend()
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    onAttachClick = { /* Step 4c */ }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.fillMaxSize()
            ) {
                items(count = items.itemCount) { idx ->
                    val msg = items[idx] ?: return@items
                    val prev = if (idx + 1 < items.itemCount) items[idx + 1] else null

                    MessageBubble(
                        message = msg,
                        onRetry = if (msg.status == MessageStatus.FAILED)
                            { { viewModel.onRetry(msg.id) } } else null
                    )

                    val msgDate = msg.sentAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    val prevDate = prev?.sentAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    if (prevDate == null || prevDate != msgDate) {
                        DateSeparator(label = TimeFormatter.chatHeader(msgDate))
                    }
                }
                if (items.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
