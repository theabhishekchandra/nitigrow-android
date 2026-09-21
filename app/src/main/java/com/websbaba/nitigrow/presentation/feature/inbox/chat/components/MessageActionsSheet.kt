package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.Theme
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// MessageActionsSheet — bottom sheet shown after the user long-presses a bubble
// in the chat thread.
// See docs/phase-3-mobile.md "Message Actions (long press)".
//
// Action set (matches WhatsApp):
//   Reply  · Copy · Forward · Delete · Star · Info
// Outbound bubbles show all 6. Inbound bubbles omit Delete (you can only delete
// your own messages from your view — same as WhatsApp's UX).
// ─────────────────────────────────────────────────────────────────────────────

/** Strongly-typed actions a user can invoke from the long-press sheet. */
sealed interface MessageAction {
    data object Reply : MessageAction
    data object Copy : MessageAction
    data object Forward : MessageAction
    data object Delete : MessageAction
    data object Star : MessageAction
    data object Info : MessageAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsSheet(
    message: Message,
    isOutbound: Boolean,
    onAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Theme.colors.card
    ) {
        MessageActionsContent(
            message = message,
            isOutbound = isOutbound,
            onAction = { action ->
                onAction(action)
                onDismiss()
            }
        )
    }
}

@Composable
private fun MessageActionsContent(
    message: Message,
    isOutbound: Boolean,
    onAction: (MessageAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 12.dp)
    ) {
        MessagePreviewHeader(message = message, isOutbound = isOutbound)
        Spacer(Modifier.padding(top = 8.dp))

        val actions = buildList {
            add(ActionRowSpec(MessageAction.Reply, Icons.AutoMirrored.Filled.Reply, "Reply"))
            add(ActionRowSpec(MessageAction.Copy, Icons.Filled.ContentCopy, "Copy"))
            add(ActionRowSpec(MessageAction.Forward, Icons.AutoMirrored.Filled.Send, "Forward"))
            add(ActionRowSpec(MessageAction.Star, Icons.Filled.Star, "Star"))
            add(ActionRowSpec(MessageAction.Info, Icons.Filled.Info, "Info"))
            // Delete only for outbound — matches WhatsApp's UX (you cannot
            // "delete for me" on inbound messages without also losing context).
            if (isOutbound) add(ActionRowSpec(MessageAction.Delete, Icons.Filled.Delete, "Delete", destructive = true))
        }

        actions.forEach { spec ->
            ActionRow(spec = spec, onClick = { onAction(spec.action) })
        }
    }
}

private data class ActionRowSpec(
    val action: MessageAction,
    val icon: ImageVector,
    val label: String,
    val destructive: Boolean = false
)

@Composable
private fun ActionRow(spec: ActionRowSpec, onClick: () -> Unit) {
    val colors = Theme.colors
    val ink = if (spec.destructive) colors.danger else colors.ink
    val tint = if (spec.destructive) colors.danger else colors.ink2

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = spec.icon,
            contentDescription = spec.label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.padding(start = 16.dp))
        Text(
            text = spec.label,
            style = MaterialTheme.typography.bodyLarge,
            color = ink,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MessagePreviewHeader(message: Message, isOutbound: Boolean) {
    val colors = Theme.colors
    val accentColor = if (isOutbound) colors.brand else colors.accent
    val author = if (isOutbound) "You" else "Customer"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.paper2)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 4dp colour bar
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 36.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(Modifier.padding(start = 10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = author,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message.text.ifBlank { "[${message.type.name.lowercase()}]" },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "MessageActions — outbound (all 6)", showBackground = true)
@Composable
private fun PreviewMessageActionsOutbound() {
    NitiGrowTheme {
        MessageActionsContent(
            message = previewMessage(outbound = true),
            isOutbound = true,
            onAction = {}
        )
    }
}

@Preview(name = "MessageActions — inbound (no Delete)", showBackground = true)
@Composable
private fun PreviewMessageActionsInbound() {
    NitiGrowTheme {
        MessageActionsContent(
            message = previewMessage(outbound = false),
            isOutbound = false,
            onAction = {}
        )
    }
}

private fun previewMessage(outbound: Boolean) = Message(
    id = "m-preview",
    conversationId = "c-preview",
    text = if (outbound) "Sharing the catalog now." else "Could you share the GST invoice?",
    sentAt = Instant.now(),
    outbound = outbound,
    status = MessageStatus.DELIVERED,
    type = MessageType.TEXT
)
