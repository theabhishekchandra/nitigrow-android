package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.presentation.feature.inbox.list.components.StatusTicks
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private val outgoingBg = Color(0xFFDCF8C6)   // WhatsApp light green
private val incomingBg = Color(0xFFFFFFFF)

@Composable
fun MessageBubble(
    message: Message,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isOut = message.outbound
    val align = if (isOut) Alignment.End else Alignment.Start
    val bg = if (isOut) outgoingBg else incomingBg
    val shape = if (isOut)
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    else
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = align
    ) {
        val clickModifier = if (message.status == MessageStatus.FAILED && onRetry != null)
            Modifier.clickable { onRetry() }
        else Modifier

        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(shape)
                .background(bg)
                .then(clickModifier)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Spacer(Modifier.padding(top = 2.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (message.status == MessageStatus.FAILED) {
                    Text(
                        text = "Tap to retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.padding(start = 6.dp))
                }
                Text(
                    text = timeFmt.format(message.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF667781)
                )
                if (isOut) {
                    Spacer(Modifier.padding(start = 4.dp))
                    StatusTicks(status = message.status)
                }
            }
        }
    }
}
