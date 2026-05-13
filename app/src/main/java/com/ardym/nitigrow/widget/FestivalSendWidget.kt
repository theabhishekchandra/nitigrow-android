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
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.Button
import com.ardym.nitigrow.MainActivity

/**
 * Home-screen widget that nudges the agent to send a festival greeting before
 * the next major Indian festival lands. The button just launches the app for
 * now — the parent will route to the festival template picker once the route
 * is added to the nav-graph.
 */
class FestivalSendWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: Replace [DummyWidgetData.upcomingFestival] with a real backend
        //  call once `/api/calendar/festivals` is live.
        val festival = DummyWidgetData.upcomingFestival()
        provideContent {
            GlanceTheme {
                FestivalSendContent(festival)
            }
        }
    }
}

class FestivalSendReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FestivalSendWidget()
}

@Composable
private fun FestivalSendContent(festival: Festival) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
    ) {
        WidgetHeader(title = "Festival greetings")
        Spacer(GlanceModifier.height(8.dp))
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = festival.name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = countdownLabel(festival.daysUntil),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        val context = LocalContext.current
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_FESTIVAL_NAME, festival.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        Button(
            text = "Send now",
            onClick = actionStartActivity(
                intent = intent,
                parameters = actionParametersOf(FestivalNameParam to festival.name)
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

private fun countdownLabel(daysUntil: Int): String = when {
    daysUntil <= 0 -> "Today!"
    daysUntil == 1 -> "Tomorrow"
    else -> "in $daysUntil days"
}
