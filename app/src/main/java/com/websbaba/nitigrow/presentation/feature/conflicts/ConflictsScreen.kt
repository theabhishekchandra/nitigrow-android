package com.websbaba.nitigrow.presentation.feature.conflicts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.feature.conflicts.components.ConflictCard
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Theme
import java.time.Instant
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────────────────────────────────────
// ConflictsScreen — surfaces the offline-sync divergences detected on
// reconnect. Phase-3-mobile.md Section 4.2 spells the rules; this screen lets
// the agent override per-item or accept the default.
// ─────────────────────────────────────────────────────────────────────────────

private val ListContentPadding = 16.dp
private val ItemSpacing = 12.dp
private val EmptyIconSize = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictsScreen(
    onBack: () -> Unit,
    viewModel: ConflictViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sync conflicts",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Theme.colors.brand,
                    titleContentColor = Theme.colors.paper,
                    navigationIconContentColor = Theme.colors.paper,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Theme.colors.paper),
        ) {
            if (state.conflicts.isEmpty()) {
                EmptyState()
            } else {
                ConflictList(
                    state = state,
                    onResolve = viewModel::resolve,
                )
            }
        }
    }
}

@Composable
private fun ConflictList(
    state: ConflictUiState,
    onResolve: (id: String, action: ConflictAction) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(ListContentPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "explainer") {
            ExplainerBanner(conflictCount = state.conflicts.size)
        }
        items(items = state.conflicts, key = { it.id }) { conflict ->
            ConflictCard(
                conflict = conflict,
                isResolving = state.isResolving,
                onResolve = { action -> onResolve(conflict.id, action) },
            )
        }
    }
}

@Composable
private fun ExplainerBanner(conflictCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Theme.colors.brandSoft)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "$conflictCount items need your attention",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.brandInk,
            )
            BulletLine("Server-timestamp wins for edits.")
            BulletLine("Deletions on web override local pending edits.")
            BulletLine("You can override per item below.")
        }
    }
}

@Composable
private fun BulletLine(text: String) {
    Text(
        text = "•  $text",
        style = MaterialTheme.typography.bodySmall,
        color = Theme.colors.brandInk,
    )
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CloudDone,
            contentDescription = null, // decorative — paired with text below
            tint = Theme.colors.success,
            modifier = Modifier.size(EmptyIconSize),
        )
        Box(modifier = Modifier.size(16.dp))
        Text(
            text = "All synced. No conflicts.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Theme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.size(8.dp))
        Text(
            text = "When you make changes offline and they collide with the server, you'll see them here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "ConflictsScreen — populated", widthDp = 360, heightDp = 720)
@Composable
private fun ConflictsScreenPopulatedPreview() {
    NitiGrowTheme {
        Box(modifier = Modifier.background(Theme.colors.paper)) {
            ConflictList(
                state = ConflictUiState(
                    conflicts = listOf(
                        SyncConflict(
                            id = "1",
                            entityType = "contact",
                            entityName = "Priya Sharma",
                            detectedAt = Instant.now().minus(2, ChronoUnit.HOURS),
                            description = "You edited tags offline. Web also changed tags.",
                            localValue = "Tags: Lead, Hot",
                            serverValue = "Tags: Lead, VIP",
                            rule = ConflictRule.SERVER_WINS,
                        ),
                    ),
                ),
                onResolve = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true, name = "ConflictsScreen — empty")
@Composable
private fun ConflictsScreenEmptyPreview() {
    NitiGrowTheme {
        Box(
            modifier = Modifier
                .background(Theme.colors.paper)
                .fillMaxSize()
                .padding(32.dp),
        ) {
            EmptyState()
        }
    }
}
