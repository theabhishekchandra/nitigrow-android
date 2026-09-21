package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.core.ui.theme.BubbleInShape
import com.websbaba.nitigrow.core.ui.theme.BubbleOutShape
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/** Fraction of the screen a bubble may occupy, per the design (~78%). */
private const val BUBBLE_MAX_WIDTH_FRACTION = 0.78f

/**
 * Colours for one bubble direction. [panel] is the translucent fill for
 * controls and cards nested inside the bubble (buttons, file rows, quotes).
 */
@Immutable
data class BubbleColors(
    val container: Color,
    val content: Color,
    val meta: Color,
    val panel: Color,
)

/** Outbound bubbles are brand-filled; inbound sit on a warm neutral. */
@Composable
fun bubbleColors(isOutbound: Boolean): BubbleColors {
    val c = Niti.colors
    return if (isOutbound) {
        BubbleColors(
            container = c.bubbleOut,
            content = c.onBubbleOut,
            meta = c.onBubbleOut.copy(alpha = 0.85f),
            panel = c.onBubbleOut.copy(alpha = 0.18f),
        )
    } else {
        BubbleColors(
            container = c.bubbleIn,
            content = c.onBubbleIn,
            meta = c.onSurfaceVariant,
            panel = c.surface.copy(alpha = 0.55f),
        )
    }
}

/**
 * Delivery ticks for use INSIDE an outbound bubble, where the list-row tints
 * would vanish against the brand surface. Shared by every bubble type so tick
 * styling stays consistent.
 */
@Composable
fun BubbleStatusTicks(status: MessageStatus, modifier: Modifier = Modifier) {
    val c = Niti.colors
    val (icon, tint) = when (status) {
        MessageStatus.PENDING -> NitiIcons.Clock to c.onBubbleOut.copy(alpha = 0.65f)
        MessageStatus.SENT -> NitiIcons.Check to c.onBubbleOut.copy(alpha = 0.65f)
        MessageStatus.DELIVERED -> NitiIcons.DoubleCheck to c.onBubbleOut.copy(alpha = 0.65f)
        MessageStatus.READ -> NitiIcons.DoubleCheck to c.readOnBubble
        MessageStatus.FAILED -> NitiIcons.Warning to c.onBubbleOut
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name.lowercase().replaceFirstChar { it.uppercase() },
        tint = tint,
        modifier = modifier.size(16.dp)
    )
}

/** 11/14 timestamp style shared by every bubble type. */
@Composable
fun bubbleTimeStyle(): TextStyle =
    NitiType.caption.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Normal)

/** Timestamp/meta tint for the given direction. */
@Composable
fun bubbleMetaColor(isOutbound: Boolean): Color = bubbleColors(isOutbound).meta

/** Max width a bubble may take on this screen. */
@Composable
fun bubbleMaxWidth() =
    (LocalConfiguration.current.screenWidthDp * BUBBLE_MAX_WIDTH_FRACTION).dp

/** Plain text bubble. */
@Composable
fun MessageBubble(
    message: Message,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isOut = message.outbound
    val shape = if (isOut) BubbleOutShape else BubbleInShape
    val colors = bubbleColors(isOut)
    val failed = message.status == MessageStatus.FAILED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
        val clickModifier = if (failed && onRetry != null)
            Modifier.clickable(role = Role.Button, onClickLabel = "Retry sending", onClick = onRetry)
        else Modifier

        Column(
            modifier = Modifier
                .widthIn(max = bubbleMaxWidth())
                .clip(shape)
                .background(colors.container)
                .then(clickModifier)
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp)
        ) {
            Text(text = message.text, style = NitiType.body, color = colors.content)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                if (failed) {
                    Text(
                        text = "Tap to retry",
                        style = bubbleTimeStyle().copy(fontWeight = FontWeight.Medium),
                        color = colors.content
                    )
                    Spacer(Modifier.size(2.dp))
                }
                Text(text = timeFmt.format(message.sentAt), style = bubbleTimeStyle(), color = colors.meta)
                if (isOut) BubbleStatusTicks(status = message.status)
            }
        }
    }
}
