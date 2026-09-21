package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.core.ui.theme.BubbleInShape
import com.websbaba.nitigrow.core.ui.theme.BubbleOutShape
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val BubbleMaxWidth = 280.dp
private val ThumbHeight = 130.dp
private val InnerPadding = 6.dp
private val CaptionPaddingH = 10.dp
private val CaptionPaddingV = 8.dp
private val PinBadgeSize = 36.dp
private val PinIconSize = 22.dp

private val LocTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/**
 * Parses `"lat,lng"` (e.g. `"19.0760,72.8777"`) from the message text. Falls
 * back to a placeholder when the format is unexpected.
 */
private fun parseLatLng(text: String): Pair<Double, Double>? {
    val parts = text.split(",").map { it.trim() }
    if (parts.size != 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    return lat to lng
}

@Composable
fun LocationMessageBubble(
    message: Message,
    isOutbound: Boolean,
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bubble = bubbleColors(isOutbound)
    val bg = bubble.container
    val ink = bubble.content
    val metaColor = bubbleMetaColor(isOutbound = isOutbound)
    val align = if (isOutbound) Alignment.End else Alignment.Start

    val coords = parseLatLng(message.text)
    val caption = coords?.let { "%.4f, %.4f".format(it.first, it.second) } ?: message.text

    val gridColor = ink.copy(alpha = 0.18f)
    val landColor = Niti.colors.secondaryTone.container

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = align,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = BubbleMaxWidth)
                .clip(shape)
                .background(bg)
                .clickable { onOpenMaps() }
                .padding(InnerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ThumbHeight)
                    .clip(shape)
                    .background(landColor),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 24.dp.toPx()
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f,
                        )
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                        )
                        y += step
                    }
                }
                Box(
                    modifier = Modifier
                        .size(PinBadgeSize)
                        .clip(CircleShape)
                        .background(Niti.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Pin",
                        tint = Color.White,
                        modifier = Modifier.size(PinIconSize),
                    )
                }
            }

            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CaptionPaddingH, vertical = CaptionPaddingV),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = caption.ifBlank { "Shared location" },
                    style = NitiType.caption,
                    color = ink,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LocTimeFmt.format(message.sentAt),
                        style = bubbleTimeStyle(),
                        color = metaColor,
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

@Preview(name = "Location — Outbound")
@Composable
private fun PreviewLocationOut() {
    NitiGrowTheme {
        LocationMessageBubble(
            message = Message(
                id = "l1",
                conversationId = "c1",
                text = "19.0760, 72.8777",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.DELIVERED,
                type = MessageType.LOCATION,
            ),
            isOutbound = true,
            onOpenMaps = {},
        )
    }
}

@Preview(name = "Location — Inbound")
@Composable
private fun PreviewLocationIn() {
    NitiGrowTheme {
        LocationMessageBubble(
            message = Message(
                id = "l2",
                conversationId = "c1",
                text = "28.6139, 77.2090",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.LOCATION,
            ),
            isOutbound = false,
            onOpenMaps = {},
        )
    }
}
