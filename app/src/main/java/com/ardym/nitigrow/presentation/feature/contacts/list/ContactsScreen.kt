package com.ardym.nitigrow.presentation.feature.contacts.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.ardym.nitigrow.presentation.feature.contacts.list.components.AlphabetScrubber
import com.ardym.nitigrow.presentation.feature.contacts.list.components.ContactRow
import com.ardym.nitigrow.presentation.feature.contacts.list.components.ContactSheet
import com.ardym.nitigrow.presentation.feature.contacts.list.components.LetterHeader
import com.ardym.nitigrow.presentation.feature.inbox.list.components.InboxSearchBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(viewModel: ContactsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

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
        topBar = { TopAppBar(title = { Text("Contacts", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openCreate) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            InboxSearchBar(query = state.query, onQueryChange = viewModel::onQueryChange)
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        state.grouped.forEach { (letter, list) ->
                            item(key = "h-$letter") { LetterHeader(letter) }
                            items(items = list, key = { c -> "c-${c.id}" }) { c ->
                                ContactRow(contact = c, onClick = { viewModel.openEdit(c) })
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 72.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
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
            onDismiss = viewModel::closeSheet,
            onSave = viewModel::saveSheet
        )
    }
}
