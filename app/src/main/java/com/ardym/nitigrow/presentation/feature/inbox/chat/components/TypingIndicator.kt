package com.ardym.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ardym.nitigrow.ui.theme.BubbleInShape
import com.ardym.nitigrow.ui.theme.Theme

private val DotSize = 7.dp
private const val BLINK_MILLIS = 600
private const val DOT_STAGGER_MILLIS = 200

/**
 * Inbound-style bubble with three staggered blinking dots — shown at the
 * bottom of the message list while the contact is typing.
 */
@Composable
fun TypingIndicator(name: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(BubbleInShape)
            .background(Theme.colors.bubbleIn)
            .border(1.dp, Theme.colors.bubbleInBorder, BubbleInShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = "$name is typing" }
    ) {
        repeat(3) { index ->
            val dotAlpha by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = BLINK_MILLIS, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * DOT_STAGGER_MILLIS)
                ),
                label = "typingDot$index"
            )
            Box(
                modifier = Modifier
                    .size(DotSize)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(Theme.colors.muted)
            )
        }
    }
}
