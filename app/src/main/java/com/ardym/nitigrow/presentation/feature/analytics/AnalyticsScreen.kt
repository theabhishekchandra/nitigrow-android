package com.ardym.nitigrow.presentation.feature.analytics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.presentation.feature.analytics.components.LineChart
import com.ardym.nitigrow.presentation.feature.analytics.components.StatCard
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.util.Locale

private val ScreenPadding: Dp = 16.dp
private val SectionGap: Dp = 12.dp
private val CardCornerRadius: Dp = 16.dp
private val SectionTitleGap: Dp = 6.dp
private val TopTemplateLimit: Int = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    AnalyticsScreenContent(
        state = state,
        onRange = { range ->
            if (range == DateRange.CUSTOM) {
                Toast.makeText(context, "Custom range picker coming soon", Toast.LENGTH_SHORT).show()
            }
            viewModel.setRange(range)
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsScreenContent(
    state: AnalyticsUiState,
    onRange: (DateRange) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Theme.colors.brand,
                    titleContentColor = Theme.colors.paper,
                    navigationIconContentColor = Theme.colors.paper,
                ),
            )
        },
        containerColor = Theme.colors.paper,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap),
        ) {
            item { RangeChips(selected = state.range, onSelected = onRange) }
            item { StatCardsRow(state = state) }
            item { LineChartCard(state = state) }
            item { SectionTitle("Top templates") }
            items(items = state.templatePerformance.take(TopTemplateLimit)) { perf ->
                TemplatePerfRow(perf)
            }
            item { SectionTitle("Agent performance") }
            items(items = state.agentPerformance) { perf ->
                AgentPerfRow(perf)
            }
        }
    }
}

@Composable
private fun RangeChips(selected: DateRange, onSelected: (DateRange) -> Unit) {
    val labels = remember {
        listOf(
            DateRange.TODAY to "Today",
            DateRange.WEEK to "Week",
            DateRange.MONTH to "Month",
            DateRange.CUSTOM to "Custom",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { (range, label) ->
            FilterChip(
                selected = selected == range,
                onClick = { onSelected(range) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Theme.colors.brandSoft,
                    selectedLabelColor = Theme.colors.brand,
                ),
            )
        }
    }
}

@Composable
private fun StatCardsRow(state: AnalyticsUiState) {
    val nf = remember { NumberFormat.getInstance(Locale("en", "IN")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        StatCard(
            title = "Sent",
            value = nf.format(state.totalSent),
            subtitle = "messages",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Delivered",
            value = "${(state.deliveryRate * 100).toInt()}%",
            subtitle = nf.format(state.totalDelivered),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Read",
            value = "${(state.readRate * 100).toInt()}%",
            subtitle = nf.format(state.totalRead),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LineChartCard(state: AnalyticsUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        shape = RoundedCornerShape(CardCornerRadius),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Daily sent vs read",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Theme.colors.ink,
            )
            Spacer(Modifier.height(SectionTitleGap))
            LineChart(points = state.timeSeries)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Theme.colors.ink,
        modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 4.dp),
    )
}

@Composable
private fun TemplatePerfRow(perf: TemplatePerf) {
    val nf = remember { NumberFormat.getInstance(Locale("en", "IN")) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        shape = RoundedCornerShape(CardCornerRadius),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = perf.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                )
                Text(
                    text = "${(perf.readRate * 100).toInt()}% read",
                    style = MaterialTheme.typography.labelMedium,
                    color = Theme.colors.brand,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(SectionTitleGap))
            LinearProgressIndicator(
                progress = { perf.readRate.coerceIn(0f, 1f) },
                color = Theme.colors.brand,
                trackColor = Theme.colors.paper3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
            Spacer(Modifier.height(SectionTitleGap))
            Text(
                text = "${nf.format(perf.sent)} sent",
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
            )
        }
    }
}

@Composable
private fun AgentPerfRow(perf: AgentPerf) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Theme.colors.card),
        shape = RoundedCornerShape(CardCornerRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarInitial(name = perf.name)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = perf.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                )
                Text(
                    text = "${perf.chats} chats · avg ${formatResponse(perf.avgResponseSeconds)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.colors.muted,
                )
            }
        }
    }
}

private fun formatResponse(seconds: Int): String =
    if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"

@Composable
private fun AvatarInitial(name: String) {
    val initials = name.split(" ").take(2).map { it.firstOrNull()?.uppercaseChar() ?: ' ' }.joinToString("")
    val palette = Theme.colors.avatars
    val pair = palette[(name.hashCode() and Int.MAX_VALUE) % palette.size]
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(pair.first),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = pair.second,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    NitiGrowTheme {
        AnalyticsScreenContent(
            state = AnalyticsUiState(
                range = DateRange.WEEK,
                timeSeries = listOf(
                    DailyPoint(java.time.LocalDate.now().minusDays(2), sent = 120, delivered = 110, read = 70),
                    DailyPoint(java.time.LocalDate.now().minusDays(1), sent = 90, delivered = 85, read = 52),
                    DailyPoint(java.time.LocalDate.now(), sent = 140, delivered = 132, read = 88)
                ),
                agentPerformance = listOf(
                    AgentPerf(name = "Priya", chats = 24, avgResponseSeconds = 90),
                    AgentPerf(name = "Aman", chats = 18, avgResponseSeconds = 140)
                )
            ),
            onRange = {},
            onBack = {},
        )
    }
}
