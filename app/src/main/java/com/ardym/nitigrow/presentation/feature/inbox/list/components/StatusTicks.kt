package com.ardym.nitigrow.presentation.feature.inbox.list.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.MessageStatus

@Composable
fun StatusTicks(status: MessageStatus, modifier: Modifier = Modifier) {
    val tint = when (status) {
        MessageStatus.READ -> Color(0xFF34B7F1)         // WhatsApp blue
        MessageStatus.FAILED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
        modifier = modifier.size(16.dp)
    )
}
