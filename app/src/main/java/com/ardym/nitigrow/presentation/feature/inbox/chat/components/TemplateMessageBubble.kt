package com.ardym.nitigrow.presentation.feature.inbox.chat.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

private val BubbleMaxWidth = 320.dp
private val HeaderHeight = 160.dp
private val InnerPadding = 4.dp
private val ContentPadding = 12.dp
private val ButtonGap = 6.dp
private val ButtonHeight = 40.dp
private val MaxButtons = 3
private const val TITLE_SEPARATOR = "\n\n"

private val TemplateTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/**
 * Decoded template content. Convention (stub until backend lands):
 *  - Line 1 → title (bold header)
 *  - Blank line separator
 *  - Remaining lines → body, EXCEPT lines starting with `"[btn]"` which are
 *    quick-reply button labels.
 */
private data class TemplatePayload(
    val title: String,
    val body: String,
    val buttons: List<String>,
)

private fun decodeTemplate(text: String): TemplatePayload {
    val sections = text.split(TITLE_SEPARATOR, limit = 2)
    val title = sections.getOrNull(0).orEmpty().trim()
    val rest = sections.getOrNull(1).orEmpty()
    val lines = rest.lines()
    val buttons = lines
        .filter { it.trim().startsWith("[btn]") }
        .map { it.removePrefix("[btn]").trim() }
        .filter { it.isNotEmpty() }
        .take(MaxButtons)
    val body = lines
        .filterNot { it.trim().startsWith("[btn]") }
        .joinToString("\n")
        .trim()
    return TemplatePayload(title, body, buttons)
}

@Composable
fun TemplateMessageBubble(
    message: Message,
    isOutbound: Boolean,
    onButtonClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val shape = if (isOutbound) BubbleOutShape else BubbleInShape
    val bg = if (isOutbound) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOutbound) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val metaColor = bubbleMetaColor(isOutbound = isOutbound)
    val align = if (isOutbound) Alignment.End else Alignment.Start

    val payload = decodeTemplate(message.text)

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
            if (!message.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Template header",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HeaderHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Theme.colors.paper2),
                )
                Spacer(Modifier.height(ContentPadding))
            }

            Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
                if (payload.title.isNotBlank()) {
                    Text(
                        text = payload.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                }
                if (payload.body.isNotBlank()) {
                    Text(
                        text = payload.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ink,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = TemplateTimeFmt.format(message.sentAt),
                        style = bubbleTimeStyle(),
                        color = metaColor,
                    )
                    if (isOutbound) {
                        Spacer(Modifier.padding(start = 4.dp))
                        BubbleStatusTicks(status = message.status)
                    }
                }
            }

            if (payload.buttons.isNotEmpty()) {
                Spacer(Modifier.height(ContentPadding))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = InnerPadding),
                    verticalArrangement = Arrangement.spacedBy(ButtonGap),
                ) {
                    payload.buttons.forEach { label ->
                        TemplateButton(label = label, onClick = { onButtonClick(label) })
                    }
                }
                Spacer(Modifier.height(InnerPadding))
            }
        }
    }
}

@Composable
private fun TemplateButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Theme.colors.border, RoundedCornerShape(10.dp))
            .background(Theme.colors.card)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.OpenInNew,
                contentDescription = null,
                tint = Theme.colors.brand,
                modifier = Modifier.padding(end = 6.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Theme.colors.brand,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview(name = "Template — Outbound")
@Composable
private fun PreviewTemplateOut() {
    NitiGrowTheme {
        TemplateMessageBubble(
            message = Message(
                id = "t1",
                conversationId = "c1",
                text = "Order Confirmed\n\nThanks for your order #1024. Your package will arrive by tomorrow.\n[btn]Track order\n[btn]Contact support",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.DELIVERED,
                type = MessageType.TEMPLATE,
                mediaUrl = "https://picsum.photos/600/300",
                mediaMimeType = "image/jpeg",
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Template — Inbound")
@Composable
private fun PreviewTemplateIn() {
    NitiGrowTheme {
        TemplateMessageBubble(
            message = Message(
                id = "t2",
                conversationId = "c1",
                text = "Welcome\n\nThanks for joining NitiGrow.\n[btn]Get started",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.TEMPLATE,
            ),
            isOutbound = false,
        )
    }
}
