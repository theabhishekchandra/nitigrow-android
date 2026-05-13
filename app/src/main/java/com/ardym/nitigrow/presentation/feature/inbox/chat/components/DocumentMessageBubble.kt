package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

private val BubbleMaxWidth = 320.dp
private val IconBoxSize = 44.dp
private val IconSize = 28.dp
private val HorizontalPadding = 12.dp
private val VerticalPadding = 10.dp
private val GapBetween = 12.dp

private val DocTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@Composable
fun DocumentMessageBubble(
    message: Message,
    isOutbound: Boolean,
    onOpenDoc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bg = if (isOutbound) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOutbound) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val align = if (isOutbound) Alignment.End else Alignment.Start

    val mime = message.mediaMimeType.orEmpty()
    val icon = if (mime.contains("pdf", ignoreCase = true))
        Icons.Filled.PictureAsPdf else Icons.Filled.Description

    val context = LocalContext.current
    val sizeLabel = message.mediaSizeBytes?.let { Formatter.formatShortFileSize(context, it) }
    val filename = message.text.ifBlank { message.mediaUrl?.substringAfterLast('/') ?: "Document" }

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
                .clickable { onOpenDoc() }
                .padding(horizontal = HorizontalPadding, vertical = VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(IconBoxSize)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ink.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Document",
                    tint = ink,
                    modifier = Modifier.size(IconSize),
                )
            }
            Spacer(Modifier.size(GapBetween))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = filename,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ink,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = sizeLabel ?: mime.ifBlank { "Document" },
                        style = MaterialTheme.typography.labelSmall,
                        color = ink.copy(alpha = 0.7f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = DocTimeFmt.format(message.sentAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = ink.copy(alpha = 0.7f),
                        )
                        if (isOutbound) {
                            Spacer(Modifier.size(4.dp))
                            StatusTicks(status = message.status)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Document — Outbound")
@Composable
private fun PreviewDocOut() {
    NitiGrowTheme {
        DocumentMessageBubble(
            message = Message(
                id = "d1",
                conversationId = "c1",
                text = "Invoice-2026-001.pdf",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.DELIVERED,
                type = MessageType.DOCUMENT,
                mediaUrl = "https://example.com/inv.pdf",
                mediaMimeType = "application/pdf",
                mediaSizeBytes = 248_320,
            ),
            isOutbound = true,
            onOpenDoc = {},
        )
    }
}

@Preview(name = "Document — Inbound")
@Composable
private fun PreviewDocIn() {
    NitiGrowTheme {
        DocumentMessageBubble(
            message = Message(
                id = "d2",
                conversationId = "c1",
                text = "Quotation.docx",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.DOCUMENT,
                mediaUrl = "https://example.com/q.docx",
                mediaMimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                mediaSizeBytes = 84_500,
            ),
            isOutbound = false,
            onOpenDoc = {},
        )
    }
}
