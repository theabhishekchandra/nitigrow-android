package com.websbaba.nitigrow.presentation.feature.contacts.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.AlphabetScrubber
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.ContactRow
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.ContactSheet
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.LetterHeader
import com.websbaba.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// ContactsScreen — CRM contact list (design: "Contacts" tab).
//   Fraunces header + "N in CRM · synced X ago" + bordered Leads-board button,
//   bordered card search field, avatar rows with neutral tag pills, square
//   56dp brand FAB → Add-contact sheet. Alphabet scrubber stays functional.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    onLeadsBoard: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val colors = Theme.colors

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is ContactsEffect.ShowMessage -> snackbar.showSnackbar(e.text)
                ContactsEffect.Dismiss -> Unit
            }
        }
    }

    // Build flat ranges so scrubber can jump to letter
    val letterToFirstIndex = remember(state.items) {
        val map = mutableMapOf<Char, Int>()
        var idx = 0
        state.grouped.forEach { (letter, list) ->
            map[letter] = idx
            idx += 1 + list.size       // header + rows
        }
        map
    }

    Scaffold(
        containerColor = colors.paper,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreate,
                containerColor = colors.brand,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ContactsHeader(
                count = state.items.size,
                lastSyncedAt = state.lastSyncedAt,
                onLeadsBoard = onLeadsBoard
            )
            ContactsSearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 10.dp)
            )
            state.error?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 10.dp)
                        .background(
                            colors.danger.copy(alpha = .12f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            colors.danger.copy(alpha = .25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 13.dp, vertical = 10.dp)
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        state.grouped.forEach { (letter, list) ->
                            item(key = "h-$letter") { LetterHeader(letter) }
                            items(items = list, key = { c -> "c-${c.id}" }) { c ->
                                ContactRow(contact = c, onClick = { viewModel.openEdit(c) })
                                HorizontalDivider(thickness = 1.dp, color = colors.border2)
                            }
                        }
                    }
                    if (!state.isRefreshing && state.items.isEmpty()) {
                        EmptyContacts(
                            query = state.query,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp)
                        )
                    }
                    AlphabetScrubber(
                        letters = state.sectionLetters,
                        onLetter = { ch ->
                            letterToFirstIndex[ch]?.let { idx ->
                                scope.launch { listState.scrollToItem(idx) }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    )
                }
            }
        }
    }

    if (state.sheetOpen) {
        ContactSheet(
            editing = state.editing,
            saving = state.savingSheet,
            error = state.sheetError,
            tagOptions = state.allTags,
            onDismiss = viewModel::closeSheet,
            onSave = viewModel::saveSheet
        )
    }
}

@Composable
private fun ContactsHeader(
    count: Int,
    lastSyncedAt: Instant?,
    onLeadsBoard: () -> Unit
) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Contacts",
                style = MaterialTheme.typography.displaySmall,
                color = colors.ink
            )
            Text(
                text = buildString {
                    append("$count in CRM")
                    lastSyncedAt?.let { append(" · synced ${agoLabel(it)}") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted
            )
        }
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.card)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onLeadsBoard)
                .padding(horizontal = 13.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Leads board",
                style = MaterialTheme.typography.labelMedium,
                color = colors.ink2
            )
        }
    }
}

@Composable
private fun ContactsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.brand),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colors.card, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 13.dp)
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.size(9.dp))
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 11.dp)
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search name or number",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.muted,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun EmptyContacts(query: String, modifier: Modifier = Modifier) {
    val colors = Theme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = if (query.isBlank()) "No contacts yet" else "No contacts found",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink2
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (query.isBlank()) {
                "Tap + to add your first contact to the CRM"
            } else {
                "Try a different name or number"
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted
        )
    }
}

/** "just now" / "2 min ago" / "3 hr ago" / "2 d ago" for the header sync line. */
private fun agoLabel(at: Instant): String {
    val mins = Duration.between(at, Instant.now()).toMinutes()
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 60 * 24 -> "${mins / 60} hr ago"
        else -> "${mins / (60 * 24)} d ago"
    }
}
