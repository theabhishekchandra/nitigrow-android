package com.websbaba.nitigrow.presentation.feature.campaigns.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.campaigns.list.components.CampaignRow
import com.websbaba.nitigrow.ui.theme.Theme

// ─────────────────────────────────────────────────────────────────────────────
// CampaignsScreen — "Broadcasts" tab root.
//
//   Broadcasts                       [Templates] [Analytics]
//   ┌─ SCHEDULED · Sat 14 Jun · 10:00 AM ───────────────────┐
//   ┌─ COMPLETED · 2 Jun  ›  + SENT/DELIVERED/READ stats ───┐
//   ┌─ DRAFT (dashed border) ───────────────────────────────┐
//                                          [＋ New broadcast]
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
    val colors = Theme.colors

    Scaffold(
        containerColor = colors.paper,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    "Broadcasts",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.ink,
                    modifier = Modifier.weight(1f)
                )
                HeaderButton("Templates", onClick = onTemplates)
                HeaderButton("Analytics", onClick = onAnalytics)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                shape = RoundedCornerShape(16.dp),
                containerColor = colors.brand,
                contentColor = if (colors.isLight) colors.paper else colors.brandInk,
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Text(
                        "New broadcast",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                state.items.isEmpty() && state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorBanner(message = state.error!!)
                    }
                }
                state.items.isEmpty() && !state.isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No broadcasts yet. Tap “New broadcast” to send your first one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.muted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    state.error?.let { item { ErrorBanner(message = it) } }
                    items(items = state.items, key = { it.id }) { c ->
                        CampaignRow(campaign = c, onClick = { onCampaignClick(c.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderButton(label: String, onClick: () -> Unit) {
    val colors = Theme.colors
    val shape = RoundedCornerShape(12.dp)
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = colors.ink2,
        modifier = Modifier
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
    )
}
