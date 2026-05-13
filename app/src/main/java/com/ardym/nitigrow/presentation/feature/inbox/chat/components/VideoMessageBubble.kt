package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.presentation.feature.inbox.list.components.StatusTicks
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.BubbleOutShape
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val BubbleMaxWidth = 280.dp
private val ThumbHeight = 200.dp
private val InnerPadding = 4.dp
private val CaptionPadding = 8.dp
private val MetaSpacing = 4.dp
private val PlayBadgeSize = 56.dp
private val PlayIconSize = 36.dp

private val VideoTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@Composable
fun VideoMessageBubble(
    message: Message,
    isOutbound: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bg = if (isOutbound) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOutbound) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val align = if (isOutbound) Alignment.End else Alignment.Start

    var launched by remember { mutableStateOf(false) }

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
                .padding(InnerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ThumbHeight)
                    .clip(shape)
                    .background(Color.Black)
                    .clickable { launched = true },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Video thumbnail",
                    modifier = Modifier.fillMaxWidth().height(ThumbHeight),
                )
                Box(
                    modifier = Modifier
                        .size(PlayBadgeSize)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(PlayIconSize),
                    )
                }
            }

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
                    text = VideoTimeFmt.format(message.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = ink.copy(alpha = 0.7f),
                )
                if (isOutbound) {
                    Spacer(Modifier.padding(start = MetaSpacing))
                    StatusTicks(status = message.status)
                }
            }
        }

        if (launched) {
            // ExoPlayer wiring is out of scope for this sprint — show a card placeholder
            // so the dispatching behaviour can be verified visually. The parent screen
            // will replace this with a real player route.
            Text(
                text = "Opening video player...",
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Preview(name = "Video — Outbound")
@Composable
private fun PreviewVideoOut() {
    NitiGrowTheme {
        VideoMessageBubble(
            message = Message(
                id = "v1",
                conversationId = "c1",
                text = "Product demo",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.DELIVERED,
                type = MessageType.VIDEO,
                mediaUrl = "https://picsum.photos/600/400",
                mediaMimeType = "video/mp4",
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Video — Inbound")
@Composable
private fun PreviewVideoIn() {
    NitiGrowTheme {
        VideoMessageBubble(
            message = Message(
                id = "v2",
                conversationId = "c1",
                text = "",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.VIDEO,
                mediaUrl = "https://picsum.photos/600/400",
                mediaMimeType = "video/mp4",
            ),
            isOutbound = false,
        )
    }
}
