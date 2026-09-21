package com.websbaba.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.core.util.TimeFormatter
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.presentation.feature.inbox.list.windowExpiryLabel
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType

/**
 * One chat in the inbox: avatar, name + time, delivery ticks + preview, and an
 * unread badge. Unread rows get heavier type and a primary-coloured time.
 * While searching, [query] matches are highlighted in the name and preview.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    query: String = "",
) {
    val colors = Niti.colors
    val unread = conversation.unreadCount > 0
    val expiryLabel = conversation.windowExpiryLabel()
    val highlight = SpanStyle(
        background = colors.secondaryTone.container,
        color = colors.secondaryTone.onContainer,
    )
    val name = remember(conversation.contactName, query, colors) {
        highlighted(conversation.contactName, query, highlight)
    }
    // A phone-number hit has nothing to highlight in the message, so show the number.
    val previewText = if (query.isNotBlank() &&
        !conversation.lastMessage.contains(query, ignoreCase = true) &&
        conversation.contactPhone.contains(query, ignoreCase = true)
    ) conversation.contactPhone else conversation.lastMessage
    val preview = remember(previewText, query, colors) { highlighted(previewText, query, highlight) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .combinedClickable(
                role = Role.Button,
                onClickLabel = "Open chat",
                onClick = onClick,
                onLongClickLabel = if (conversation.isPinned) "Unpin chat" else "Pin chat",
                onLongClick = onLongClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = conversation.contactName, url = conversation.avatarUrl, sizeDp = 48)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = NitiType.titleUi.copy(
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (expiryLabel != null) {
                    Icon(
                        imageVector = NitiIcons.Clock,
                        contentDescription = "Chat window closes in $expiryLabel",
                        tint = colors.badge,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = TimeFormatter.listLabel(conversation.lastMessageAt),
                    style = NitiType.caption.copy(
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (unread) colors.primary else colors.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.lastMessageOutbound) {
                        DeliveryTicks(
                            status = conversation.lastMessageStatus,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = preview,
                        style = NitiType.bodyCompact.copy(
                            fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (unread) colors.onSurface else colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (conversation.isMuted) {
                    Icon(
                        imageVector = Icons.Filled.VolumeOff,
                        contentDescription = "Muted",
                        tint = colors.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (conversation.isPinned) {
                    Icon(
                        imageVector = NitiIcons.Pin,
                        contentDescription = "Pinned",
                        tint = colors.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (unread) UnreadCount(conversation.unreadCount)
            }
        }
    }
}

/** Sent ✓, delivered ✓✓ (muted), read ✓✓ (blue), pending clock, failed warning. */
@Composable
private fun DeliveryTicks(status: MessageStatus, modifier: Modifier = Modifier) {
    val colors = Niti.colors
    val (icon, tint) = when (status) {
        MessageStatus.PENDING -> NitiIcons.Clock to colors.outline
        MessageStatus.SENT -> NitiIcons.Check to colors.outline
        MessageStatus.DELIVERED -> NitiIcons.DoubleCheck to colors.outline
        MessageStatus.READ -> NitiIcons.DoubleCheck to colors.read
        MessageStatus.FAILED -> NitiIcons.Warning to colors.error
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name.lowercase().replaceFirstChar { it.uppercase() },
        tint = tint,
        modifier = modifier.size(18.dp)
    )
}

@Composable
private fun UnreadCount(count: Int) {
    val colors = Niti.colors
    Text(
        text = if (count > 99) "99+" else count.toString(),
        style = NitiType.caption.copy(fontWeight = FontWeight.SemiBold),
        color = colors.onPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .background(colors.primary, CircleShape)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

/** [text] with every case-insensitive occurrence of [query] styled by [style]. */
internal fun highlighted(text: String, query: String, style: SpanStyle): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val hit = text.indexOf(needle, cursor, ignoreCase = true)
            if (hit < 0) break
            append(text.substring(cursor, hit))
            withStyle(style) { append(text.substring(hit, hit + needle.length)) }
            cursor = hit + needle.length
        }
        append(text.substring(cursor))
    }
}
