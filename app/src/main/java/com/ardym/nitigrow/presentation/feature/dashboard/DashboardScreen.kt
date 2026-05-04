package com.ardym.nitigrow.presentation.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.domain.model.DashboardStats
import com.ardym.nitigrow.presentation.components.ErrorBanner
import com.ardym.nitigrow.presentation.feature.dashboard.components.DeliveryReadCard
import com.ardym.nitigrow.presentation.feature.dashboard.components.StatCard
import com.ardym.nitigrow.presentation.feature.dashboard.components.StatCardRow
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                state.isInitialLoading -> InitialLoading()
                state.stats != null -> StatsList(state.stats!!, error = state.error)
                state.error != null -> ErrorView(state.error!!, onRetry = viewModel::refresh)
                else -> InitialLoading()
            }
        }
    }
}

@Composable
private fun InitialLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ErrorBanner(message = message)
        Spacer(Modifier.height(16.dp))
        Text("Pull down to retry", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatsList(stats: DashboardStats, error: String?) {
    val nf = NumberFormat.getInstance(Locale("en", "IN"))
    val cardSections: List<@Composable () -> Unit> = listOf(
        {
            StatCardRow(
                cards = listOf(
                    { mod ->
                        StatCard(
                            title = "Sent",
                            value = nf.format(stats.messagesSent),
                            icon = Icons.Filled.Send,
                            modifier = mod
                        )
                    },
                    { mod ->
                        StatCard(
                            title = "Delivered",
                            value = nf.format(stats.messagesDelivered),
                            icon = Icons.Filled.Send,
                            modifier = mod,
                            subtitle = "${(stats.deliveryRate * 100).toInt()}%"
                        )
                    }
                )
            )
        },
        {
            StatCardRow(
                cards = listOf(
                    { mod ->
                        StatCard(
                            title = "Leads",
                            value = nf.format(stats.leadsTotal),
                            icon = Icons.Filled.Group,
                            modifier = mod,
                            subtitle = "+${nf.format(stats.leadsNew)} new"
                        )
                    },
                    { mod ->
                        StatCard(
                            title = "Campaigns",
                            value = stats.activeCampaigns.toString(),
                            icon = Icons.Filled.Campaign,
                            modifier = mod,
                            subtitle = "active"
                        )
                    }
                )
            )
        },
        {
            StatCard(
                title = "Revenue (last 30d)",
                value = "₹${nf.format(stats.revenueInr)}",
                icon = Icons.Filled.CurrencyRupee,
                subtitle = "attributed to WhatsApp campaigns"
            )
        },
        {
            DeliveryReadCard(
                deliveryRate = stats.deliveryRate,
                readRate = stats.readRate
            )
        }
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        error?.let {
            item { ErrorBanner(message = it) }
        }
        items(cardSections.size) { idx -> cardSections[idx]() }
    }
}
