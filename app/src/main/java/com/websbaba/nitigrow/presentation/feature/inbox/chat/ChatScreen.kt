package com.websbaba.nitigrow.presentation.feature.inbox.chat

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.websbaba.nitigrow.core.util.TimeFormatter
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.ChatHeaderPresence
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.DateSeparator
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.MessageBubble
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.MessageInput
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.RichMessageBubble
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.TypingIndicator
import com.websbaba.nitigrow.presentation.feature.inbox.chat.components.WindowPill
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

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
        containerColor = Theme.colors.paper2,
        topBar = {
            ChatHeader(
                contactName = state.contactName,
                contactPhone = state.contactPhone,
                avatarUrl = state.avatarUrl,
                typing = state.typing,
                windowExpiresAt = state.windowExpiresAt,
                onBack = onBack
            )
        },
        bottomBar = {
            MessageInput(
                text = state.draft,
                onTextChange = viewModel::onDraftChange,
                onSend = {
                    viewModel.onSend()
                    scope.launch { listState.animateScrollToItem(0) }
                },
                onAttachClick = { /* Step 4c */ }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Bottom of the reversed list — typing bubble while the contact types.
            if (state.typing) {
                item(key = "typing-indicator") {
                    TypingIndicator(name = state.contactName.ifBlank { "Contact" })
                }
            }
            items(count = items.itemCount) { idx ->
                val msg = items[idx] ?: return@items
                val prev = if (idx + 1 < items.itemCount) items[idx + 1] else null

                if (msg.type == MessageType.TEXT && msg.status == MessageStatus.FAILED) {
                    MessageBubble(
                        message = msg,
                        onRetry = { viewModel.onRetry(msg.id) }
                    )
                } else {
                    RichMessageBubble(message = msg, isOutbound = msg.outbound)
                }

                val msgDate = msg.sentAt.atZone(ZoneId.systemDefault()).toLocalDate()
                val prevDate = prev?.sentAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
                if (prevDate == null || prevDate != msgDate) {
                    DateSeparator(label = TimeFormatter.chatHeader(msgDate))
                }
            }
            if (items.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Theme.colors.brand,
                            trackColor = Theme.colors.brandSoft
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom chat top bar — paper surface with a bottom hairline: back arrow,
 * 38dp avatar, contact name over phone/presence, and the live window pill.
 */
@Composable
private fun ChatHeader(
    contactName: String,
    contactPhone: String,
    avatarUrl: String?,
    typing: Boolean,
    windowExpiresAt: Instant?,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Theme.colors.paper).statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Theme.colors.ink,
                    modifier = Modifier.size(21.dp)
                )
            }
            Avatar(
                name = contactName.ifBlank { "?" },
                url = avatarUrl,
                sizeDp = 38
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contactName.ifBlank { "Conversation" },
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ChatHeaderPresence(
                    typing = typing,
                    lastSeen = null,
                    fallback = contactPhone
                )
            }
            WindowPill(windowExpiresAt = windowExpiresAt)
        }
        HorizontalDivider(thickness = 1.dp, color = Theme.colors.border)
    }
}
