package com.ardym.nitigrow.presentation.feature.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.leads.components.LeadCard
import com.ardym.nitigrow.presentation.feature.leads.components.next
import com.ardym.nitigrow.presentation.feature.leads.components.previous

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(viewModel: LeadsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Leads", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.error?.let { ErrorBanner(message = it, modifier = Modifier.padding(16.dp)) }
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = LeadStage.entries.toList(), key = { it.name }) { stage ->
                        StageColumn(
                            stage = stage,
                            leads = state.grouped[stage].orEmpty(),
                            onMove = viewModel::onMove
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageColumn(
    stage: LeadStage,
    leads: List<Lead>,
    onMove: (String, LeadStage) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(8.dp)
    ) {
        Text(
            "${stage.label} (${leads.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        if (leads.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No leads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = leads, key = { it.id }) { lead ->
                    LeadCard(
                        lead = lead,
                        onMovePrev = stage.previous()?.let { prev -> { onMove(lead.id, prev) } },
                        onMoveNext = stage.next()?.let { next -> { onMove(lead.id, next) } }
                    )
                }
            }
        }
    }
}
