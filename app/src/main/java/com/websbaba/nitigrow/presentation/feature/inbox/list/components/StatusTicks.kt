package com.websbaba.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.core.ui.theme.Theme

/**
 * Outbound delivery ticks: ✓ sent, ✓✓ delivered (muted), ✓✓ read (info blue).
 * Tints are overridable so bubbles rendered on the brand surface can swap in
 * on-brand colours (see BubbleStatusTicks in chat/components/MessageBubble.kt).
 */
@Composable
fun StatusTicks(
    status: MessageStatus,
    modifier: Modifier = Modifier,
    readTint: Color = Theme.colors.info,
    neutralTint: Color = Theme.colors.muted2,
    failedTint: Color = Theme.colors.danger,
) {
    val tint = when (status) {
        MessageStatus.READ -> readTint
        MessageStatus.FAILED -> failedTint
        else -> neutralTint
    }
    val icon = when (status) {
        MessageStatus.PENDING -> Icons.Filled.Schedule
        MessageStatus.SENT -> Icons.Filled.Done
        MessageStatus.DELIVERED, MessageStatus.READ -> Icons.Filled.DoneAll
        MessageStatus.FAILED -> Icons.Filled.ErrorOutline
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = modifier.size(15.dp)
    )
}
