package com.ardym.nitigrow.presentation.feature.analytics.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.presentation.feature.analytics.DailyPoint
import com.ardym.nitigrow.ui.theme.Theme

private val ChartHeight: Dp = 200.dp
private val PathStrokeWidthDp: Dp = 2.5.dp
private val GridStrokeWidthDp: Dp = 1.dp
private val GridLineCount: Int = 4
private val MaxXLabels: Int = 7
private val LegendSwatchSize: Dp = 10.dp

/**
 * Two-line analytics chart — `sent` in brand, `read` in accent.
 *
 * The polylines are drawn as Compose `Path`s once and revealed left-to-right
 * via `clipRect` driven by an animated `progress` float, so the redraw cost
 * is just the clip rectangle change per frame.
 */
@Composable
fun LineChart(
    points: List<DailyPoint>,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(points) { visible = true }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "lineChartReveal",
    )

    val brand = Theme.colors.brand
    val accent = Theme.colors.accent
    val grid = Theme.colors.border
    val muted = Theme.colors.muted

    Column(modifier = modifier.fillMaxWidth()) {
        Legend(brand = brand, accent = accent)
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight),
        ) {
            if (points.isEmpty()) return@Canvas

            val maxValue = (points.maxOf { maxOf(it.sent, it.read) }.coerceAtLeast(1)).toFloat()
            val w = size.width
            val h = size.height
            val topPad = 8f
            val bottomPad = 8f
            val plotW = w
            val plotH = h - topPad - bottomPad

            // Horizontal grid lines
            val gridStroke = GridStrokeWidthDp.toPx()
            for (i in 0..GridLineCount) {
                val y = topPad + plotH * (i.toFloat() / GridLineCount)
                drawLine(
                    color = grid,
                    start = Offset(0f, y),
                    end = Offset(plotW, y),
                    strokeWidth = gridStroke,
                )
            }

            val sentPath = buildPath(points.map { it.sent.toFloat() }, plotW, plotH, topPad, maxValue)
            val readPath = buildPath(points.map { it.read.toFloat() }, plotW, plotH, topPad, maxValue)

            val stroke = Stroke(width = PathStrokeWidthDp.toPx(), cap = StrokeCap.Round)

            // Progress-clip the drawing region for a left-to-right reveal.
            clipRect(right = plotW * progress) {
                drawPath(path = sentPath, color = brand, style = stroke)
                drawPath(path = readPath, color = accent, style = stroke)
            }
        }
        Spacer(Modifier.height(6.dp))
        XAxisLabels(points = points, muted = muted)
    }
}

private fun buildPath(
    values: List<Float>,
    plotW: Float,
    plotH: Float,
    topPad: Float,
    maxValue: Float,
): Path {
    val path = Path()
    if (values.isEmpty()) return path
    val n = values.size
    val stepX = if (n > 1) plotW / (n - 1) else 0f
    values.forEachIndexed { i, v ->
        val x = stepX * i
        val y = topPad + plotH - (v / maxValue) * plotH
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

@Composable
private fun Legend(brand: Color, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Swatch(color = brand)
        Spacer(Modifier.size(4.dp))
        Text(
            text = "Sent",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Theme.colors.ink2,
        )
        Spacer(Modifier.size(14.dp))
        Swatch(color = accent)
        Spacer(Modifier.size(4.dp))
        Text(
            text = "Read",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Theme.colors.ink2,
        )
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        modifier = Modifier
            .size(LegendSwatchSize)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun XAxisLabels(points: List<DailyPoint>, muted: Color) {
    if (points.isEmpty()) return
    val step = if (points.size <= MaxXLabels) 1 else points.size / MaxXLabels
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)) {
        points.forEachIndexed { i, p ->
            if (i % step == 0 || i == points.size - 1) {
                Text(
                    text = "${p.day.dayOfMonth}/${p.day.monthValue}",
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }
    }
}
