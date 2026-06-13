package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme

private val PillVerticalPadding = 3.dp
private val PillHorizontalPadding = 8.dp
private val PillCorner = 12.dp
private val PillOffsetY = (-10).dp
private val PillOffsetX = 12.dp

/**
 * Small floating pill that sits underneath a message bubble showing the
 * reaction emoji + count, e.g. "❤️ 3". Anchored bottom-end via `Modifier.offset`
 * by the parent — the caller decides which side (start/end) by passing the
 * appropriate horizontal offset sign.
 */
@Composable
fun ReactionPill(
    emoji: String,
    count: Int,
    isOutbound: Boolean,
    modifier: Modifier = Modifier,
) {
    val xOffset = if (isOutbound) -PillOffsetX else PillOffsetX
    Row(
        modifier = modifier
            .offset(x = xOffset, y = PillOffsetY)
            .clip(RoundedCornerShape(PillCorner))
            .background(Theme.colors.card)
            .border(1.dp, Theme.colors.border, RoundedCornerShape(PillCorner))
            .padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelMedium,
        )
        if (count > 1) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Theme.colors.ink2,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview(name = "Reaction — Outbound")
@Composable
private fun PreviewReactionOut() {
    NitiGrowTheme {
        ReactionPill(emoji = "❤️", count = 3, isOutbound = true)
    }
}

@Preview(name = "Reaction — Inbound (single)")
@Composable
private fun PreviewReactionIn() {
    NitiGrowTheme {
        ReactionPill(emoji = "👍", count = 1, isOutbound = false)
    }
}
