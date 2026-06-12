package com.ardym.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardym.nitigrow.core.util.TimeFormatter
import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.presentation.feature.inbox.list.windowExpiryLabel
import com.ardym.nitigrow.ui.theme.Theme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unread = conversation.unreadCount > 0
    val expiryLabel = conversation.windowExpiryLabel()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            name = conversation.contactName,
            url = conversation.avatarUrl,
            sizeDp = 46
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Name + time on the first baseline row.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.contactName,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (conversation.isPinned) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(13.dp),
                        tint = Theme.colors.muted2
                    )
                }
                if (conversation.isMuted) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.VolumeOff,
                        contentDescription = "Muted",
                        modifier = Modifier.size(13.dp),
                        tint = Theme.colors.muted2
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = TimeFormatter.listLabel(conversation.lastMessageAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Theme.colors.muted2
                )
            }
            Spacer(Modifier.height(2.dp))
            // Ticks + preview + unread / expiring pills on the second row.
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (conversation.lastMessageOutbound) {
                    StatusTicks(
                        status = conversation.lastMessageStatus,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = if (unread) Theme.colors.ink else Theme.colors.muted,
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (unread) {
                    Spacer(Modifier.width(6.dp))
                    UnreadBadge(count = conversation.unreadCount)
                }
                if (expiryLabel != null) {
                    Spacer(Modifier.width(6.dp))
                    ExpiringPill(label = expiryLabel)
                }
            }
        }
    }
}

@Composable
private fun ExpiringPill(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = 0.sp
        ),
        fontWeight = FontWeight.Bold,
        color = Theme.colors.turmericInk,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Theme.colors.turmericSoft)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
