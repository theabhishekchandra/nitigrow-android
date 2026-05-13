package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// InternalNoteBubble — yellow/turmeric-tinted bubble for agent-only notes.
// See docs/phase-3-mobile.md "Internal Notes".
//
// Notes are visible to the agent team in the conversation timeline but are
// never sent to the WhatsApp customer. The UI distinguishes them clearly:
//   • turmericSoft background instead of cream paper or brand green
//   • warning-tinted lock icon to signal "private"
//   • explicit "Internal note" label + author name
//   • dashed-feel hairline border in warning hue
// ─────────────────────────────────────────────────────────────────────────────

private val NoteTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@Composable
fun InternalNoteBubble(
    message: Message,
    authorName: String,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(shape)
                .background(colors.turmericSoft)
                .border(width = 1.dp, color = colors.warning.copy(alpha = 0.45f), shape = shape)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Internal note",
                    tint = colors.warning,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Internal note",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.warning,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.muted
                )
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink3,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.padding(top = 6.dp))

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.ink
            )

            Spacer(Modifier.padding(top = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Visible to team only · ${NoteTimeFormatter.format(message.sentAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "InternalNoteBubble — short", showBackground = true)
@Composable
private fun PreviewInternalNoteBubbleShort() {
    NitiGrowTheme {
        InternalNoteBubble(
            message = previewNote("Customer is hesitant on price — try offering 5% bulk discount."),
            authorName = "Sneha (Sales)"
        )
    }
}

@Preview(name = "InternalNoteBubble — long", showBackground = true)
@Composable
private fun PreviewInternalNoteBubbleLong() {
    NitiGrowTheme {
        InternalNoteBubble(
            message = previewNote(
                "Spoke to Priya over phone — wants Diwali catalog by Friday. Also asked for GST invoice format. " +
                    "I'll prepare the wholesale rate sheet and share by tomorrow EOD."
            ),
            authorName = "Pankaj (Owner)"
        )
    }
}

private fun previewNote(text: String) = Message(
    id = "note-preview",
    conversationId = "c-preview",
    text = text,
    sentAt = Instant.now(),
    outbound = true,
    status = MessageStatus.SENT,
    type = MessageType.TEXT
)

@Suppress("UnusedPrivateProperty")
private val PreviewBackgroundColor: Color = Color.Transparent
