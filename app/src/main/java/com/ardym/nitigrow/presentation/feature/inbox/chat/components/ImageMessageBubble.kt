package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.BubbleOutShape
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Layout constants — kept local to avoid magic numbers downstream.
private val BubbleMaxWidth = 280.dp
private val ThumbHeight = 200.dp
private val InnerPadding = 4.dp
private val CaptionPadding = 8.dp
private val MetaSpacing = 4.dp

private val ImageTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@Composable
fun ImageMessageBubble(
    message: Message,
    isOutbound: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bg = if (isOutbound) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOutbound) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val align = if (isOutbound) Alignment.End else Alignment.Start

    var fullScreen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = align,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = BubbleMaxWidth)
                .clip(shape)
                .background(bg)
                .border(1.dp, if (isOutbound) bg else Theme.colors.bubbleInBorder, shape)
                .padding(InnerPadding),
        ) {
            AsyncImage(
                model = message.mediaUrl,
                contentDescription = "Image attachment",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ThumbHeight)
                    .clip(shape)
                    .background(Theme.colors.paper2)
                    .pointerInput(message.id) {
                        detectTransformGestures { _, _, _, _ -> fullScreen = true }
                    },
            )

            if (message.text.isNotBlank()) {
                Spacer(Modifier.height(MetaSpacing))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ink,
                    modifier = Modifier.padding(horizontal = CaptionPadding),
                )
            }

            Spacer(Modifier.height(MetaSpacing))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CaptionPadding, vertical = MetaSpacing),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ImageTimeFmt.format(message.sentAt),
                    style = bubbleTimeStyle(),
                    color = bubbleMetaColor(isOutbound = isOutbound),
                )
                if (isOutbound) {
                    Spacer(Modifier.padding(start = MetaSpacing))
                    BubbleStatusTicks(status = message.status)
                }
            }
        }
    }

    if (fullScreen) {
        Dialog(
            onDismissRequest = { fullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            FullScreenImageViewer(
                url = message.mediaUrl,
                onDismiss = { fullScreen = false },
            )
        }
    }
}

@Composable
private fun FullScreenImageViewer(url: String?, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Full-screen image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                ),
        )
        Text(
            text = "Tap outside to close",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        )
    }
}

@Preview(name = "Image — Outbound")
@Composable
private fun PreviewImageOut() {
    NitiGrowTheme {
        ImageMessageBubble(
            message = Message(
                id = "m1",
                conversationId = "c1",
                text = "Here's the brochure",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.READ,
                type = MessageType.IMAGE,
                mediaUrl = "https://picsum.photos/600/400",
                mediaMimeType = "image/jpeg",
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Image — Inbound")
@Composable
private fun PreviewImageIn() {
    NitiGrowTheme {
        ImageMessageBubble(
            message = Message(
                id = "m2",
                conversationId = "c1",
                text = "",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.IMAGE,
                mediaUrl = "https://picsum.photos/600/400",
                mediaMimeType = "image/jpeg",
            ),
            isOutbound = false,
        )
    }
}
