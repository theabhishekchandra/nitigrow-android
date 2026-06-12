package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.presentation.feature.inbox.list.components.StatusTicks
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val StickerSize = 128.dp
private val StickerTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/**
 * Stickers render edge-to-edge with no bubble background (matches WhatsApp).
 * When `mediaUrl` is present we render it via Coil; otherwise the message
 * `text` is treated as a single large emoji.
 */
@Composable
fun StickerMessageBubble(
    message: Message,
    isOutbound: Boolean,
    modifier: Modifier = Modifier,
) {
    val align = if (isOutbound) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = align,
    ) {
        Box(
            modifier = Modifier.size(StickerSize),
            contentAlignment = Alignment.Center,
        ) {
            if (!message.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Sticker",
                    modifier = Modifier.size(StickerSize),
                )
            } else {
                Text(
                    text = message.text.ifBlank { "😀" },
                    style = TextStyle(fontSize = 96.sp),
                )
            }
        }

        Spacer(Modifier.size(2.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = StickerTimeFmt.format(message.sentAt),
                style = bubbleTimeStyle(),
                color = Theme.colors.muted2,
            )
            if (isOutbound) {
                Spacer(Modifier.size(4.dp))
                StatusTicks(status = message.status)
            }
        }
    }
}

@Preview(name = "Sticker — Outbound (emoji)")
@Composable
private fun PreviewStickerEmoji() {
    NitiGrowTheme {
        StickerMessageBubble(
            message = Message(
                id = "s1",
                conversationId = "c1",
                text = "🎉",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Sticker — Inbound (image)")
@Composable
private fun PreviewStickerImage() {
    NitiGrowTheme {
        StickerMessageBubble(
            message = Message(
                id = "s2",
                conversationId = "c1",
                text = "",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.TEXT,
                mediaUrl = "sticker://thumbsup",
                mediaMimeType = "image/webp",
            ),
            isOutbound = false,
        )
    }
}
