package com.websbaba.nitigrow.presentation.feature.analytics

import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.util.formatIndian
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val ScreenPadding: Dp = 18.dp
private val SectionGap: Dp = 14.dp
private val CardCorner: Dp = 16.dp
private val KpiGap: Dp = 10.dp
private val ChartHeight: Dp = 110.dp
private val BarGap: Dp = 9.dp

/** Header period presets mapped onto the existing [DateRange] model. */
private enum class AnalyticsPeriod(val label: String, val range: DateRange) {
    SEVEN_DAYS("7D", DateRange.WEEK),
    THIRTY_DAYS("30D", DateRange.MONTH),
    NINETY_DAYS("90D", DateRange.QUARTER),
}

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnalyticsScreenContent(
        state = state,
        onRange = viewModel::setRange,
        onBack = onBack,
    )
}

@Composable
private fun AnalyticsScreenContent(
    state: AnalyticsUiState,
    onRange: (DateRange) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AnalyticsHeader(
                selectedRange = state.range,
                onPeriod = onRange,
                onBack = onBack,
            )
        },
        containerColor = Niti.colors.surface,
    ) { padding ->
        when {
            state.isLoading && state.timeSeries.isEmpty() ->
                AnalyticsSkeleton(modifier = Modifier.padding(padding))

            state.timeSeries.isEmpty() ->
                AnalyticsEmptyState(modifier = Modifier.padding(padding))

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = ScreenPadding,
                    end = ScreenPadding,
                    top = 8.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(SectionGap),
            ) {
                item { KpiRow(state = state) }
                item { MessageVolumeCard(points = state.timeSeries) }
                item { FunnelCard(state = state) }
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsHeader(
    selectedRange: DateRange,
    onPeriod: (DateRange) -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 2.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Niti.colors.onSurface,
            )
        }
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = Niti.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        AnalyticsPeriod.entries.forEach { period ->
            PeriodPill(
                label = period.label,
                selected = selectedRange == period.range,
                onClick = { onPeriod(period.range) },
            )
        }
    }
}

@Composable
private fun PeriodPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (selected) Niti.colors.surface else Niti.colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Niti.colors.primary else Niti.colors.surfaceLow)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

// ── KPI cards ───────────────────────────────────────────────────────────────

@Composable
private fun KpiRow(state: AnalyticsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KpiGap),
    ) {
        KpiCard(
            label = "MESSAGES SENT",
            value = formatIndian(state.totalSent),
            modifier = Modifier.weight(1f),
        )
        // AVG RESPONSE TIME is intentionally omitted: the analytics API exposes
        // no response-time metric. The spacer keeps the two-column grid rhythm.
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Niti.colors.surfaceLow, RoundedCornerShape(CardCorner))
            .border(1.dp, Niti.colors.outlineVariant, RoundedCornerShape(CardCorner))
            .padding(14.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = Niti.colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Niti.colors.onSurface,
        )
    }
}

// ── Message volume bar chart ────────────────────────────────────────────────

private data class VolumeBar(val label: String, val value: Int)

/**
 * Buckets the daily series so the chart never overflows: daily bars for week
 * windows, weekly buckets for month windows, monthly buckets for 90-day windows.
 */
private fun buildVolumeBars(points: List<DailyPoint>): List<VolumeBar> {
    if (points.isEmpty()) return emptyList()
    val sorted = points.sortedBy { it.day }
    return when {
        sorted.size <= 7 -> sorted.map { point ->
            VolumeBar(
                label = point.day.dayOfWeek.getDisplayName(DateTextStyle.NARROW, Locale.ENGLISH),
                value = point.sent,
            )
        }

        sorted.size <= 35 -> sorted.chunked(7).mapIndexed { index, week ->
            VolumeBar(label = "W${index + 1}", value = week.sumOf { it.sent })
        }

        else -> sorted
            .groupBy { YearMonth.from(it.day) }
            .map { (month, days) ->
                VolumeBar(
                    label = month.month.getDisplayName(DateTextStyle.SHORT, Locale.ENGLISH),
                    value = days.sumOf { it.sent },
                )
            }
    }
}

@Composable
private fun MessageVolumeCard(points: List<DailyPoint>) {
    val bars = remember(points) { buildVolumeBars(points) }
    AnalyticsCard(label = "MESSAGE VOLUME", labelGap = 14.dp) {
        if (bars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No messages in this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Niti.colors.onSurfaceVariant,
                )
            }
        } else {
            // The design highlights the most recent COMPLETE period — the
            // second-to-last bar (the last one is still in progress).
            val highlightIndex = (bars.size - 2).coerceAtLeast(0)
            val maxValue = bars.maxOf { it.value }.coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight),
                horizontalArrangement = Arrangement.spacedBy(BarGap),
            ) {
                bars.forEachIndexed { index, bar ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        val fraction =
                            if (bar.value <= 0) 0f
                            else (bar.value.toFloat() / maxValue).coerceIn(0.03f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .background(
                                    color = if (index == highlightIndex) {
                                        Niti.colors.primary
                                    } else {
                                        Niti.colors.primary.copy(alpha = 0.25f)
                                    },
                                    shape = RoundedCornerShape(
                                        topStart = 6.dp,
                                        topEnd = 6.dp,
                                        bottomStart = 3.dp,
                                        bottomEnd = 3.dp,
                                    ),
                                ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BarGap),
            ) {
                bars.forEach { bar ->
                    Text(
                        text = bar.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Niti.colors.outline,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── Funnel ──────────────────────────────────────────────────────────────────

@Composable
private fun FunnelCard(state: AnalyticsUiState) {
    AnalyticsCard(label = "FUNNEL", labelGap = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FunnelStat(
                label = "DELIVERED",
                value = formatIndian(state.totalDelivered),
                pct = pctOfSent(state.totalDelivered, state.totalSent),
                background = Niti.colors.surfaceLow,
                valueColor = Niti.colors.onSurface,
                labelColor = Niti.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            FunnelStat(
                label = "READ",
                value = formatIndian(state.totalRead),
                pct = pctOfSent(state.totalRead, state.totalSent),
                background = Niti.colors.secondaryTone.container,
                valueColor = Niti.colors.secondaryTone.onContainer,
                labelColor = Niti.colors.secondaryTone.onContainer,
                modifier = Modifier.weight(1f),
            )
            // REPLIES is intentionally omitted: the analytics API has no
            // total-replies metric for the selected period (top-agents reply
            // counts are capped at 10 agents over a fixed 30-day window).
        }
    }
}

@Composable
private fun FunnelStat(
    label: String,
    value: String,
    pct: String,
    background: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$label · $pct",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}

private fun pctOfSent(part: Int, sent: Int): String =
    if (sent == 0) "0%" else "${(part * 100f / sent).roundToInt()}%"

// ── Shared card shell ───────────────────────────────────────────────────────

@Composable
private fun AnalyticsCard(
    label: String,
    labelGap: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Niti.colors.surfaceLow, RoundedCornerShape(CardCorner))
            .border(1.dp, Niti.colors.outlineVariant, RoundedCornerShape(CardCorner))
            .padding(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Niti.colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(labelGap))
        content()
    }
}

// ── Loading / empty states ──────────────────────────────────────────────────

@Composable
private fun AnalyticsSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "analyticsSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "skeletonPulse",
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding, vertical = 8.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(KpiGap)) {
            SkeletonBlock(modifier = Modifier.weight(1f).height(78.dp))
            SkeletonBlock(modifier = Modifier.weight(1f).height(78.dp))
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(170.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(96.dp))
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Niti.colors.surfaceContainer, RoundedCornerShape(CardCorner)))
}

@Composable
private fun AnalyticsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(Niti.colors.surfaceLow, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = null,
                tint = Niti.colors.outlineVariant,
                modifier = Modifier.size(46.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No analytics yet",
            style = MaterialTheme.typography.headlineSmall,
            color = Niti.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Send your first broadcast and delivery stats will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Niti.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    NitiGrowTheme {
        AnalyticsScreenContent(
            state = AnalyticsUiState(
                range = DateRange.WEEK,
                timeSeries = listOf(38, 52, 61, 44, 70, 58, 66).mapIndexed { index, sent ->
                    DailyPoint(
                        day = LocalDate.now().minusDays((6 - index).toLong()),
                        sent = sent,
                        delivered = (sent * 0.96f).roundToInt(),
                        read = (sent * 0.70f).roundToInt(),
                    )
                },
            ),
            onRange = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenEmptyPreview() {
    NitiGrowTheme {
        AnalyticsScreenContent(
            state = AnalyticsUiState(timeSeries = emptyList(), isLoading = false),
            onRange = {},
            onBack = {},
        )
    }
}
