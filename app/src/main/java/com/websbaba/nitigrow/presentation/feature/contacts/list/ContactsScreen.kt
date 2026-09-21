package com.websbaba.nitigrow.presentation.feature.contacts.list

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.NitiChip
import com.websbaba.nitigrow.presentation.components.NitiExtendedFab
import com.websbaba.nitigrow.presentation.components.NitiIconButton
import com.websbaba.nitigrow.presentation.components.NitiSearchField
import com.websbaba.nitigrow.presentation.components.NitiStateView
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.AlphabetScrubber
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.ContactRow
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.ContactSheet
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.LetterHeader
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// ContactsScreen — CRM contact list (design: "Contacts" tab).
//   sync button · "Contacts" + "1,240 in CRM · synced 2 min ago"
//   Leads board card · search · tag chips
//   A–Z sectioned rows with tag pills · A–Z scrubber · [＋ Add contact]
// ─────────────────────────────────────────────────────────────────────────────

private val nf: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

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
    val colors = Niti.colors

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is ContactsEffect.ShowMessage -> snackbar.showSnackbar(e.text)
                ContactsEffect.Dismiss -> Unit
            }
        }
    }

    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    // Flat index of each letter's header so the scrubber can jump to it.
    val grouped = state.grouped
    val letterToFirstIndex = remember(grouped) {
        val map = mutableMapOf<Char, Int>()
        var idx = 0
        grouped.forEach { (letter, list) ->
            map[letter] = idx
            idx += 1 + list.size // header + rows
        }
        map
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp)
            ) {
                NitiIconButton(
                    icon = NitiIcons.Refresh,
                    contentDescription = "Sync contacts",
                    onClick = viewModel::refresh
                )
            }
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)) {
                Text(text = "Contacts", style = NitiType.display.copy(letterSpacing = (-0.8).sp), color = colors.onSurface)
                Text(
                    text = crmSubtitle(state.items.size, state.lastSyncedAt),
                    style = NitiType.bodyCompact,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            LeadsBoardCard(leadsTotal = state.leadsTotal, leadsNew = state.leadsNew, onClick = onLeadsBoard)
            NitiSearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = "Search name or number",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            )
            if (state.tagChips.isNotEmpty()) {
                TagChips(
                    tags = state.tagChips,
                    selected = state.tagFilter,
                    onSelect = viewModel::onTagFilter
                )
            }
            state.error?.let { message ->
                if (state.items.isNotEmpty()) {
                    Text(
                        text = message,
                        style = NitiType.label,
                        color = colors.error,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.error.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
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
                    state.items.isEmpty() && state.isRefreshing -> ContactsSkeleton()
                    state.items.isEmpty() && state.error != null -> NitiStateView(
                        icon = NitiIcons.Warning,
                        tone = colors.tertiaryTone,
                        title = "Couldn't load contacts",
                        body = state.error.orEmpty(),
                        actionLabel = "Retry",
                        onAction = viewModel::refresh
                    )
                    state.items.isEmpty() -> NitiStateView(
                        icon = NitiIcons.Contacts,
                        tone = colors.secondaryTone,
                        title = if (state.query.isBlank()) "No contacts yet" else "No contacts found",
                        body = if (state.query.isBlank()) "Tap Add contact to add your first contact to the CRM."
                        else "Try a different name or number."
                    )
                    grouped.isEmpty() -> NitiStateView(
                        icon = NitiIcons.Star,
                        tone = colors.secondaryTone,
                        title = "No contacts with this tag",
                        body = "Tap the tag again to see everyone."
                    )
                    else -> Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            grouped.forEach { (letter, list) ->
                                item(key = "h-$letter") { LetterHeader(letter) }
                                items(count = list.size, key = { i -> "c-${list[i].id}" }) { i ->
                                    val c = list[i]
                                    ContactRow(contact = c, onClick = { viewModel.openEdit(c) })
                                }
                            }
                        }
                        AlphabetScrubber(
                            letters = state.sectionLetters,
                            onLetter = { ch ->
                                letterToFirstIndex[ch]?.let { idx -> scope.launch { listState.scrollToItem(idx) } }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                        )
                    }
                }
            }
        }

        NitiExtendedFab(
            text = "Add contact",
            icon = NitiIcons.Plus,
            onClick = viewModel::openCreate,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)
        )
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
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
private fun LeadsBoardCard(leadsTotal: Long?, leadsNew: Long?, onClick: () -> Unit) {
    val tone = Niti.colors.tertiaryTone
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(tone.container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Niti.colors.surface)
        ) {
            Icon(NitiIcons.Leads, contentDescription = null, tint = Niti.colors.badge, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Leads board", style = NitiType.bodyStrong.copy(fontWeight = FontWeight.SemiBold), color = tone.onContainer)
            Text(
                text = leadsSubtitle(leadsTotal, leadsNew),
                style = NitiType.label.copy(fontWeight = FontWeight.Normal),
                color = tone.onContainer
            )
        }
        Icon(NitiIcons.ChevronRight, contentDescription = null, tint = tone.onContainer, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TagChips(tags: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        NitiChip(label = "All", selected = selected == null, onClick = { onSelect(null) })
        tags.forEach { tag ->
            NitiChip(
                label = tag,
                selected = tag.equals(selected, ignoreCase = true),
                onClick = { onSelect(tag) },
                icon = if (tag.equals("VIP", ignoreCase = true)) NitiIcons.Star else null
            )
        }
    }
}

@Composable
private fun ContactsSkeleton() {
    val colors = Niti.colors
    val pulse by rememberInfiniteTransition(label = "contactsSkeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "contactsSkeletonAlpha"
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp).alpha(pulse)
    ) {
        repeat(6) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(colors.surfaceContainer))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.5f).heightIn(min = 14.dp).clip(RoundedCornerShape(7.dp)).background(colors.surfaceContainer))
                    Box(Modifier.fillMaxWidth(0.35f).heightIn(min = 12.dp).clip(RoundedCornerShape(6.dp)).background(colors.surfaceLow))
                }
            }
        }
    }
}

/** "1,240 in CRM · synced 2 min ago"; the sync part appears once a refresh has succeeded. */
internal fun crmSubtitle(count: Int, lastSyncedAt: Instant?, now: Instant = Instant.now()): String = buildString {
    append("${nf.format(count)} in CRM")
    lastSyncedAt?.let { append(" · synced ${agoLabel(it, now)}") }
}

/** "148 leads · 12 new this week" — falls back to a generic line until stats are cached. */
internal fun leadsSubtitle(total: Long?, new: Long?): String = when {
    total == null -> "Your lead pipeline"
    new != null && new > 0 -> "${nf.format(total)} ${if (total == 1L) "lead" else "leads"} · ${nf.format(new)} new"
    else -> "${nf.format(total)} ${if (total == 1L) "lead" else "leads"}"
}

/** "just now" / "2 min ago" / "3 hr ago" / "2 d ago". */
internal fun agoLabel(at: Instant, now: Instant = Instant.now()): String {
    val mins = Duration.between(at, now).toMinutes()
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 60 * 24 -> "${mins / 60} hr ago"
        else -> "${mins / (60 * 24)} d ago"
    }
}
