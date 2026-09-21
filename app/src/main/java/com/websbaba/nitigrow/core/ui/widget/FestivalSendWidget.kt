package com.websbaba.nitigrow.core.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.websbaba.nitigrow.MainActivity
import com.websbaba.nitigrow.R

/**
 * Home-screen widget that nudges the agent to send a festival greeting.
 *
 * v1 ships a neutral entry point. There is no backend festival calendar yet
 * (no `/calendar/festivals` route exists), so we deliberately do NOT render a
 * festival name or countdown — a hardcoded festival list would be inaccurate
 * for users (festival dates vary by region/year). The button opens the app so
 * the agent can pick a festival template themselves. When a festival data
 * layer lands, pass `kicker` ("NEXT FESTIVAL · 28 AUG") and `title`
 * ("Raksha Bandhan in 77 days") into [FestivalSendContent] and the espresso
 * card renders them in the slots already laid out below.
 */
class FestivalSendWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            FestivalSendContent()
        }
    }
}

class FestivalSendReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FestivalSendWidget()
}

@Composable
private fun FestivalSendContent(
    kicker: String? = null,
    title: String = "Festival greetings",
    helper: String = "Greet customers on upcoming festivals"
) {
    val context = LocalContext.current
    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openApp = actionStartActivity(launchIntent)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg_card_espresso))
            .cornerRadius(22.dp)
            .padding(14.dp)
            .clickable(openApp)
    ) {
        // Glyph block + (kicker slot) + festival title.
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(38.dp)
                    .background(ImageProvider(R.drawable.widget_bg_glyph_turmeric))
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "र",
                    style = TextStyle(color = WidgetPalette.gold, fontSize = 18.sp)
                )
            }
            Spacer(GlanceModifier.width(10.dp))
            Column {
                if (kicker != null) {
                    Text(
                        text = kicker,
                        style = TextStyle(
                            color = WidgetPalette.espressoKicker,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = title,
                    style = TextStyle(
                        color = WidgetPalette.cream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(GlanceModifier.defaultWeight())
        Spacer(GlanceModifier.height(11.dp))

        // Helper text + turmeric pill button.
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = helper,
                style = TextStyle(color = WidgetPalette.espressoMuted, fontSize = 11.sp),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .background(ImageProvider(R.drawable.widget_bg_pill_turmeric))
                    .cornerRadius(999.dp)
                    .padding(horizontal = 13.dp, vertical = 6.dp)
                    .clickable(openApp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Plan a send",
                    style = TextStyle(
                        color = WidgetPalette.turmericInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
