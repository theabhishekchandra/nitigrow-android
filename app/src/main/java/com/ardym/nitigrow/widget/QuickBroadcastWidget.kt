package com.ardym.nitigrow.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
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
import com.ardym.nitigrow.MainActivity
import com.ardym.nitigrow.R

/**
 * Home-screen widget for launching a broadcast from the launcher.
 *
 * v1 ships a neutral entry point. We deliberately do NOT render
 * audience/segment counts here: the segment counts live behind the
 * authenticated `GET /contacts/segments` endpoint, and surfacing them on the
 * home screen would require a WorkManager-backed, auth-aware cache that is out
 * of scope for v1. Showing real counts is better done once that cache exists —
 * we show no number rather than a fabricated one. When that data layer lands,
 * pass segments + a contact-count label into [QuickBroadcastContent] and the
 * chip row / header count render in the slots already laid out below.
 */
class QuickBroadcastWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickBroadcastContent()
        }
    }
}

class QuickBroadcastReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickBroadcastWidget()
}

/** Segment chip data — wired up once a real segment-count cache exists. */
internal data class WidgetSegment(val label: String, val count: Int)

@Composable
private fun QuickBroadcastContent(
    segments: List<WidgetSegment> = emptyList(),
    contactCountLabel: String? = null
) {
    val context = LocalContext.current
    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openApp = actionStartActivity(launchIntent)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg_card_light))
            .cornerRadius(22.dp)
            .padding(14.dp)
            .clickable(openApp)
    ) {
        // Header: logo + title (+ right-aligned muted contact count once real data exists).
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher),
                contentDescription = "NitiGrow",
                modifier = GlanceModifier.size(22.dp)
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = "Quick broadcast",
                style = TextStyle(
                    color = WidgetPalette.ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (contactCountLabel != null) {
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = contactCountLabel,
                    style = TextStyle(
                        color = WidgetPalette.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(GlanceModifier.height(10.dp))

        // Segment chip row slot; generic supporting line until real counts exist.
        if (segments.isEmpty()) {
            Text(
                text = "Broadcast to your contact segments",
                style = TextStyle(color = WidgetPalette.muted, fontSize = 11.sp)
            )
        } else {
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                segments.forEachIndexed { index, segment ->
                    if (index > 0) Spacer(GlanceModifier.width(6.dp))
                    SegmentChip(segment)
                }
            }
        }

        Spacer(GlanceModifier.defaultWeight())
        Spacer(GlanceModifier.height(11.dp))

        // Full-width accent CTA bar.
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.widget_bg_cta_accent))
                .cornerRadius(12.dp)
                .padding(vertical = 10.dp)
                .clickable(openApp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Start a broadcast",
                style = TextStyle(
                    color = WidgetPalette.paper,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SegmentChip(segment: WidgetSegment) {
    Text(
        text = "${segment.label} · ${segment.count}",
        style = TextStyle(
            color = WidgetPalette.paper,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = GlanceModifier
            .background(ImageProvider(R.drawable.widget_bg_chip_brand))
            .cornerRadius(999.dp)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    )
}
