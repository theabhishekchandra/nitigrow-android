package com.ardym.nitigrow.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ardym.nitigrow.MainActivity

/**
 * Extras key carried by the launch intent so [MainActivity] can deep-link
 * the user to the chosen segment's confirm-broadcast flow once nav-graph
 * wiring lands.
 */
internal const val EXTRA_SEGMENT_ID = "nitigrow.widget.segment_id"
internal const val EXTRA_FESTIVAL_NAME = "nitigrow.widget.festival_name"

internal val SegmentIdParam: ActionParameters.Key<String> =
    ActionParameters.Key(EXTRA_SEGMENT_ID)

internal val FestivalNameParam: ActionParameters.Key<String> =
    ActionParameters.Key(EXTRA_FESTIVAL_NAME)

/**
 * Home-screen widget that surfaces the agent's top 3 saved segments so a
 * broadcast can be triggered from the launcher in two taps.
 */
class QuickBroadcastWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: Replace [DummyWidgetData.segments] with a real WorkManager-backed
        //  segment cache once the segments API ships.
        val segments = DummyWidgetData.segments()
        provideContent {
            GlanceTheme {
                QuickBroadcastContent(segments)
            }
        }
    }
}

class QuickBroadcastReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickBroadcastWidget()
}

@Composable
private fun QuickBroadcastContent(segments: List<WidgetSegment>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
    ) {
        WidgetHeader(title = "Quick broadcast")
        Spacer(GlanceModifier.height(6.dp))
        if (segments.isEmpty()) {
            EmptyState(message = "No saved segments yet")
        } else {
            segments.forEach { segment ->
                SegmentRow(segment = segment)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SegmentRow(segment: WidgetSegment) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_SEGMENT_ID, segment.id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clickable(
                actionStartActivity(
                    intent = intent,
                    parameters = actionParametersOf(SegmentIdParam to segment.id)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = segment.name,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = "${segment.count}",
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
internal fun WidgetHeader(title: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.primary)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
internal fun EmptyState(message: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        )
    }
}
