package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val metaColor = bubbleMetaColor(isOutbound = isOutbound)
    val align = if (isOutbound) Alignment.End else Alignment.Start

    val context = LocalContext.current
    val mediaUrl = message.mediaUrl
    val mimeType = message.mediaMimeType?.takeIf { it.isNotBlank() } ?: "video/*"

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ThumbHeight)
                    .clip(shape)
                    .background(Color.Black)
                    .clickable {
                        if (mediaUrl.isNullOrBlank()) {
                            Toast.makeText(
                                context,
                                "Video is not available",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            // No bundled player on the classpath — hand off to an
                            // external player via ACTION_VIEW with the media MIME type.
                            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(mediaUrl), mimeType)
                                addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NEW_TASK,
                                )
                            }
                            try {
                                context.startActivity(playIntent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No app found to play this video",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
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
                    style = bubbleTimeStyle(),
                    color = metaColor,
                )
                if (isOutbound) {
                    Spacer(Modifier.padding(start = MetaSpacing))
                    BubbleStatusTicks(status = message.status)
                }
            }
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
