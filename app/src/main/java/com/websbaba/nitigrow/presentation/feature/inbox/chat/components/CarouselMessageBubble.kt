package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.model.MessageType
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.StatusTicks
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.ui.theme.Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CardWidth = 220.dp
private val CardImageHeight = 120.dp
private val CardSpacing = 10.dp
private val SidePadding = 8.dp
private val ButtonHeight = 36.dp
private val CardCorner = 12.dp

private val CarouselTimeFmt =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/** Single card inside a WhatsApp-style carousel. */
data class CarouselCard(
    val imageUrl: String?,
    val title: String,
    val body: String,
    val buttonLabel: String?,
)

/**
 * Decodes the carousel payload from `message.text`. Convention until backend
 * lands:
 *  - Cards separated by `"|||"`
 *  - Fields separated by `"::"` in the order: imageUrl, title, body, button
 */
internal fun decodeCarousel(text: String): List<CarouselCard> =
    text.split("|||")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { raw ->
            val parts = raw.split("::").map { it.trim() }
            CarouselCard(
                imageUrl = parts.getOrNull(0).takeUnless { it.isNullOrBlank() },
                title = parts.getOrNull(1).orEmpty(),
                body = parts.getOrNull(2).orEmpty(),
                buttonLabel = parts.getOrNull(3).takeUnless { it.isNullOrBlank() },
            )
        }

@Composable
fun CarouselMessageBubble(
    message: Message,
    isOutbound: Boolean,
    onCardClick: (CarouselCard) -> Unit = {},
    onCardButtonClick: (CarouselCard) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cards = decodeCarousel(message.text)
    val align = if (isOutbound) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = align,
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SidePadding),
            horizontalArrangement = Arrangement.spacedBy(CardSpacing),
        ) {
            items(cards) { card ->
                CarouselCardView(
                    card = card,
                    onClick = { onCardClick(card) },
                    onButtonClick = { onCardButtonClick(card) },
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = SidePadding + 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = CarouselTimeFmt.format(message.sentAt),
                style = bubbleTimeStyle(),
                color = Theme.colors.muted2,
            )
            if (isOutbound) {
                Spacer(Modifier.width(4.dp))
                StatusTicks(status = message.status)
            }
        }
    }
}

@Composable
private fun CarouselCardView(
    card: CarouselCard,
    onClick: () -> Unit,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(CardWidth)
            .clip(RoundedCornerShape(CardCorner))
            .background(Theme.colors.card)
            .border(1.dp, Theme.colors.border, RoundedCornerShape(CardCorner))
            .clickable { onClick() },
    ) {
        if (!card.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardImageHeight)
                    .background(Theme.colors.paper2),
            )
        }
        Column(modifier = Modifier.padding(10.dp)) {
            if (card.title.isNotBlank()) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Theme.colors.ink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
            }
            if (card.body.isNotBlank()) {
                Text(
                    text = card.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Theme.colors.ink2,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
            }
            if (!card.buttonLabel.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ButtonHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Theme.colors.brandSoft)
                        .clickable { onButtonClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = card.buttonLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Theme.colors.brand,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Preview(name = "Carousel — Outbound")
@Composable
private fun PreviewCarouselOut() {
    NitiGrowTheme {
        CarouselMessageBubble(
            message = Message(
                id = "car1",
                conversationId = "c1",
                text = "https://picsum.photos/300/200?1::Premium Plan::Unlimited messages, 5 agents::Choose plan" +
                    "|||https://picsum.photos/300/200?2::Growth Plan::10k messages, 2 agents::Choose plan" +
                    "|||https://picsum.photos/300/200?3::Starter Plan::1k messages, 1 agent::Choose plan",
                sentAt = Instant.now(),
                outbound = true,
                status = MessageStatus.DELIVERED,
                type = MessageType.TEMPLATE,
            ),
            isOutbound = true,
        )
    }
}

@Preview(name = "Carousel — Inbound")
@Composable
private fun PreviewCarouselIn() {
    NitiGrowTheme {
        CarouselMessageBubble(
            message = Message(
                id = "car2",
                conversationId = "c1",
                text = "https://picsum.photos/300/200?4::Saree Collection::Hand-loom silk from Banaras::View" +
                    "|||https://picsum.photos/300/200?5::Kurta Set::Festive cotton kurtas::View",
                sentAt = Instant.now(),
                outbound = false,
                status = MessageStatus.DELIVERED,
                type = MessageType.TEMPLATE,
            ),
            isOutbound = false,
        )
    }
}
