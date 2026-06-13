package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.StatusTicks
import com.websbaba.nitigrow.ui.theme.BubbleInShape
import com.websbaba.nitigrow.ui.theme.BubbleOutShape
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/** Read-tick blue tuned for legibility on the brand-green outbound bubble. */
private val ReadTickOnBrand = Color(0xFF8FD8FF)

/** Fraction of the screen a bubble may occupy, per the design (~78%). */
private const val BUBBLE_MAX_WIDTH_FRACTION = 0.78f

/**
 * Delivery ticks recoloured for use INSIDE an outbound bubble, where the
 * default info-blue/muted tints would vanish against the brand surface.
 * Shared by every bubble type so tick styling stays consistent.
 */
@Composable
fun BubbleStatusTicks(status: MessageStatus, modifier: Modifier = Modifier) {
    StatusTicks(
        status = status,
        modifier = modifier,
        readTint = ReadTickOnBrand,
        neutralTint = Theme.colors.bubbleOutInk.copy(alpha = 0.65f),
        failedTint = Theme.colors.bubbleOutInk
    )
}

/** 10sp untracked timestamp style shared by every bubble type. */
@Composable
fun bubbleTimeStyle(): TextStyle =
    MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp)

/** Timestamp/meta tint: translucent paper on the brand surface, muted2 inbound. */
@Composable
fun bubbleMetaColor(isOutbound: Boolean): Color =
    if (isOutbound) Theme.colors.bubbleOutInk.copy(alpha = 0.65f) else Theme.colors.muted2

/** Plain text bubble — brand/paper outbound, bordered white inbound. */
@Composable
fun MessageBubble(
    message: Message,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isOut = message.outbound
    val shape = if (isOut) BubbleOutShape else BubbleInShape
    val bg = if (isOut) Theme.colors.bubbleOut else Theme.colors.bubbleIn
    val ink = if (isOut) Theme.colors.bubbleOutInk else Theme.colors.bubbleInInk
    val borderColor = if (isOut) Theme.colors.bubbleOut else Theme.colors.bubbleInBorder
    val metaColor = bubbleMetaColor(isOutbound = isOut)
    val maxBubbleWidth =
        (LocalConfiguration.current.screenWidthDp * BUBBLE_MAX_WIDTH_FRACTION).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
        val clickModifier = if (message.status == MessageStatus.FAILED && onRetry != null)
            Modifier.clickable { onRetry() }
        else Modifier

        Column(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .shadow(elevation = 1.dp, shape = shape)
                .clip(shape)
                .background(bg)
                .border(1.dp, borderColor, shape)
                .then(clickModifier)
                .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 7.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = ink
            )
            Spacer(Modifier.padding(top = 3.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (message.status == MessageStatus.FAILED) {
                    Text(
                        text = "Tap to retry",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                        color = if (isOut) Theme.colors.bubbleOutInk else Theme.colors.danger,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.padding(start = 6.dp))
                }
                Text(
                    text = timeFmt.format(message.sentAt),
                    style = bubbleTimeStyle(),
                    color = metaColor
                )
                if (isOut) {
                    Spacer(Modifier.padding(start = 4.dp))
                    BubbleStatusTicks(status = message.status)
                }
            }
        }
    }
}
