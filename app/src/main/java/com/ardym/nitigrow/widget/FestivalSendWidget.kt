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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.Button
import com.ardym.nitigrow.MainActivity

/**
 * Home-screen widget that nudges the agent to send a festival greeting.
 *
 * v1 ships a neutral "Open NitiGrow" entry point. There is no backend festival
 * calendar yet (no `/calendar/festivals` route exists), so we deliberately do
 * NOT render a festival name or countdown — a hardcoded festival list would be
 * inaccurate for users (festival dates vary by region/year). The button opens
 * the app so the agent can pick a festival template themselves.
 */
class FestivalSendWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                FestivalSendContent()
            }
        }
    }
}

class FestivalSendReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FestivalSendWidget()
}

@Composable
private fun FestivalSendContent() {
    val context = LocalContext.current
    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
    ) {
        WidgetHeader(title = "Festival greetings")
        Spacer(GlanceModifier.height(8.dp))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Send a festival greeting to your contacts.",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        Button(
            text = "Open NitiGrow",
            onClick = actionStartActivity(launchIntent),
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
