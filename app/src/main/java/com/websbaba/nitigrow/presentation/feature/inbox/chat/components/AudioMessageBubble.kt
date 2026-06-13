package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.ui.theme.BubbleInShape
import com.websbaba.nitigrow.ui.theme.BubbleOutShape
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.sin
import kotlin.random.Random

private val BubbleMaxWidth = 280.dp
private val PlayButtonSize = 40.dp
private val PlayIconSize = 24.dp
private val WaveformHeight = 28.dp
private val InnerHorizontal = 10.dp
private val InnerVertical = 8.dp
private const val BARS = 32

private val AudioTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/**
 * Voice-note bubble with a stub-only waveform.
 * Duration is derived from `mediaSizeBytes` heuristically until the parent
 * agent wires a real player; until then "0:24" is the rendered fallback.
 */
@Composable
fun AudioMessageBubble(
    message: Message,
    isOutbound: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bg = if (isOutbound) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOutbound) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val align = if (isOutbound) Alignment.End else Alignment.Start

    var playing by remember { mutableStateOf(false) }

    // Deterministic pseudo-waveform per message so it does not jitter on recompose.
    val heights = remember(message.id) {
        val rng = Random(message.id.hashCode())
        List(BARS) { i ->
            val base = (sin(i * 0.45f).let { kotlin.math.abs(it) } * 0.7f) + 0.2f
            (base + rng.nextFloat() * 0.3f).coerceIn(0.15f, 1f)
        }
    }

    val durationLabel = remember(message.mediaSizeBytes) {
        val seconds = message.mediaSizeBytes?.let { (it / 8_000L).coerceIn(1, 600) }
            ?: 24L
        val mm = seconds / 60
        val ss = seconds % 60
        "%d:%02d".format(mm, ss)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = align,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = BubbleMaxWidth)
                .clip(shape)
                .background(bg)
                .border(1.dp, if (isOutbound) bg else Theme.colors.bubbleInBorder, shape)
                .padding(horizontal = InnerHorizontal, vertical = InnerVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(PlayButtonSize)
                    .clip(CircleShape)
                    .background(ink.copy(alpha = 0.12f))
                    .clickable { playing = !playing },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = ink,
                    modifier = Modifier.size(PlayIconSize),
                )
            }

            Spacer(Modifier.size(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WaveformHeight),
                ) {
                    val barCount = heights.size
                    val gap = 3.dp.toPx()
                    val barWidth = ((size.width - gap * (barCount - 1)) / barCount)
                        .coerceAtLeast(1f)
                    heights.forEachIndexed { i, h ->
                        val x = i * (barWidth + gap) + barWidth / 2f
                        val barHeight = size.height * h
                        val top = (size.height - barHeight) / 2f
                        drawLine(
                            color = ink.copy(alpha = 0.6f),
                            start = Offset(x, top),
                            end = Offset(x, top + barHeight),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = durationLabel,
                        style = bubbleTimeStyle(),
                        color = bubbleMetaColor(isOutbound = isOutbound),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AudioTimeFmt.format(message.sentAt),
                            style = bubbleTimeStyle(),
                            color = bubbleMetaColor(isOutbound = isOutbound),
                        )
                        if (isOutbound) {
                            Spacer(Modifier.size(4.dp))
                            BubbleStatusTicks(status = message.status)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Audio — Outbound")
@Composable
private fun PreviewAudioOut() {
    NitiGrowTheme {
        AudioMessageBubble(
            message = Message(
                id = "a1",
                conversationId = "c1",
                text = "",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.READ,
                type = MessageType.AUDIO,
                mediaUrl = "https://example.com/voice.ogg",
                mediaMimeType = "audio/ogg",
                mediaSizeBytes = 192_000,
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Audio — Inbound")
@Composable
private fun PreviewAudioIn() {
    NitiGrowTheme {
        AudioMessageBubble(
            message = Message(
                id = "a2",
                conversationId = "c1",
                text = "",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.AUDIO,
                mediaUrl = "https://example.com/voice.ogg",
                mediaMimeType = "audio/ogg",
                mediaSizeBytes = 96_000,
            ),
            isOutbound = false,
        )
    }
}
