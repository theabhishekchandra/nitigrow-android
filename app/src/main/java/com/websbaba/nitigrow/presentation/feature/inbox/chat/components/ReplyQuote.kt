package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// ReplyQuote + ReplyEcho — WhatsApp-style quoted-message UI.
// See docs/phase-3-mobile.md "Reply to Specific Message".
//
//   • ReplyQuote — drafting state: sits above the input bar while the user is
//     composing a reply. Has an [X] close button that cancels the reply.
//   • ReplyEcho  — sent state: a smaller, indented chip rendered *inside* the
//     outbound bubble, mimicking the way WhatsApp shows the original message
//     above the reply text.
//
// Both share the same vertical brand bar + author + truncated body layout to
// keep the visual language consistent across the input → message lifecycle.
// ─────────────────────────────────────────────────────────────────────────────

private const val QUOTE_PREVIEW_MAX_CHARS = 90

@Composable
fun ReplyQuote(
    quoted: Message,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.paper2)
            .padding(start = 0.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading 3dp brand-green vertical bar — matches WhatsApp.
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.brand)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = authorLabel(quoted),
                style = MaterialTheme.typography.labelMedium,
                color = colors.brand,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.padding(top = 2.dp))
            Text(
                text = quotePreview(quoted),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cancel reply",
                tint = colors.ink3
            )
        }
    }
}

/**
 * Smaller indented chip rendered inside an outbound bubble showing the message
 * the user is replying to. Caller is responsible for the surrounding bubble.
 */
@Composable
fun ReplyEcho(
    quoted: Message,
    isParentOutbound: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    // When the parent bubble is outbound (brand-coloured background), use a
    // translucent overlay so the chip still reads as "quoted" but stays
    // anchored visually inside the bubble.
    val chipBg = if (isParentOutbound) {
        colors.paper.copy(alpha = 0.18f)
    } else {
        colors.paper2
    }
    val barColor = if (isParentOutbound) colors.paper else colors.brand
    val authorColor = if (isParentOutbound) colors.paper else colors.brand
    val bodyColor = if (isParentOutbound) colors.paper.copy(alpha = 0.85f) else colors.ink2

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(chipBg)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .width(3.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            Text(
                text = authorLabel(quoted),
                style = MaterialTheme.typography.labelSmall,
                color = authorColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = quotePreview(quoted),
                style = MaterialTheme.typography.bodySmall,
                color = bodyColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun authorLabel(message: Message): String =
    if (message.outbound) "You" else "Customer"

private fun quotePreview(message: Message): String {
    val raw = when {
        message.text.isNotBlank() -> message.text
        message.type == MessageType.IMAGE -> "📷 Photo"
        message.type == MessageType.VIDEO -> "🎬 Video"
        message.type == MessageType.AUDIO -> "🎤 Voice note"
        message.type == MessageType.DOCUMENT -> "📄 Document"
        message.type == MessageType.LOCATION -> "📍 Location"
        message.type == MessageType.TEMPLATE -> "📋 Template"
        else -> "[message]"
    }
    return if (raw.length > QUOTE_PREVIEW_MAX_CHARS) {
        raw.take(QUOTE_PREVIEW_MAX_CHARS - 1) + "…"
    } else raw
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "ReplyQuote — drafting (inbound source)", showBackground = true)
@Composable
private fun PreviewReplyQuoteInbound() {
    NitiGrowTheme {
        ReplyQuote(
            quoted = previewQuoted(outbound = false),
            onClear = {}
        )
    }
}

@Preview(name = "ReplyQuote — drafting (outbound source, long body)", showBackground = true)
@Composable
private fun PreviewReplyQuoteOutboundLong() {
    NitiGrowTheme {
        ReplyQuote(
            quoted = previewQuoted(
                outbound = true,
                text = "Yes ma'am, fresh stock arrived this morning. ₹1,200 for 10g. Let me know how many packets you'd like."
            ),
            onClear = {}
        )
    }
}

@Preview(name = "ReplyEcho — inside inbound bubble", showBackground = true)
@Composable
private fun PreviewReplyEchoInsideInbound() {
    NitiGrowTheme {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.bubbleIn)
                .padding(8.dp)
        ) {
            Column {
                ReplyEcho(
                    quoted = previewQuoted(outbound = true),
                    isParentOutbound = false
                )
                Text(
                    "Sure — I'll send it now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Theme.colors.bubbleInInk
                )
            }
        }
    }
}

@Preview(name = "ReplyEcho — inside outbound bubble", showBackground = true)
@Composable
private fun PreviewReplyEchoInsideOutbound() {
    NitiGrowTheme {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.bubbleOut)
                .padding(8.dp)
        ) {
            Column {
                ReplyEcho(
                    quoted = previewQuoted(outbound = false),
                    isParentOutbound = true
                )
                Text(
                    "Got it — shipping today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Theme.colors.bubbleOutInk
                )
            }
        }
    }
}

private fun previewQuoted(
    outbound: Boolean,
    text: String = if (outbound) "Order confirmation sent." else "Could you share the catalog?"
) = Message(
    id = "m-quoted",
    conversationId = "c-preview",
    text = text,
    sentAt = Instant.now(),
    outbound = outbound,
    status = MessageStatus.DELIVERED,
    type = MessageType.TEXT
)

@Suppress("UnusedPrivateProperty")
private val PreviewBackgroundColor: Color = Color.Transparent
