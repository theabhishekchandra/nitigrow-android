package com.websbaba.nitigrow.presentation.feature.inbox.chat.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageType

// ─────────────────────────────────────────────────────────────────────────────
// RichMessageBubble — single entry point used by ChatScreen for every message.
//
// `MessageType` (in domain/model/Message.kt) does not yet enumerate STICKER,
// CAROUSEL or REACTION. To keep this PR additive — and avoid editing
// `MessageType.kt` per the task scope — we encode those subtypes via simple
// conventions on the existing fields:
//
//   • STICKER  → mediaMimeType starts with "image/webp" OR text starts with
//                "sticker://".  Message.type remains TEXT.
//   • CAROUSEL → message.text contains the carousel delimiter "|||" AND the
//                message.type is TEMPLATE. (A single template never carries
//                that delimiter.)
//   • REACTION → mediaMimeType == "reaction/emoji"  (rendered separately by
//                the caller, not dispatched here — kept for symmetry).
//
// When the backend model adds dedicated enum values, the convention checks
// below can be deleted and the `when` switched to plain branches.
// ─────────────────────────────────────────────────────────────────────────────

private const val STICKER_URL_PREFIX = "sticker://"
private const val STICKER_MIME_PREFIX = "image/webp"
private const val CAROUSEL_DELIMITER = "|||"
private const val REACTION_MIME = "reaction/emoji"

private fun Message.looksLikeSticker(): Boolean =
    text.startsWith(STICKER_URL_PREFIX) ||
        (mediaMimeType?.startsWith(STICKER_MIME_PREFIX) == true)

private fun Message.looksLikeCarousel(): Boolean =
    type == MessageType.TEMPLATE && text.contains(CAROUSEL_DELIMITER)

private fun Message.looksLikeReaction(): Boolean =
    mediaMimeType == REACTION_MIME

@Composable
fun RichMessageBubble(
    message: Message,
    isOutbound: Boolean,
    onOpenDoc: () -> Unit = {},
    onOpenMaps: () -> Unit = {},
    onReact: (String) -> Unit = {},
    onReply: (Message) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Subtype overrides first — these are special-cases of existing enum slots.
    if (message.looksLikeReaction()) {
        ReactionPill(
            emoji = message.text.ifBlank { "👍" },
            count = 1,
            isOutbound = isOutbound,
            modifier = modifier,
        )
        return
    }
    if (message.looksLikeSticker()) {
        StickerMessageBubble(message = message, isOutbound = isOutbound, modifier = modifier)
        return
    }
    if (message.looksLikeCarousel()) {
        CarouselMessageBubble(
            message = message,
            isOutbound = isOutbound,
            onCardClick = { onReply(message) },
            onCardButtonClick = { onReact(it.buttonLabel.orEmpty()) },
            modifier = modifier,
        )
        return
    }

    when (message.type) {
        MessageType.TEXT -> MessageBubble(message = message, modifier = modifier)
        MessageType.IMAGE -> ImageMessageBubble(message, isOutbound, modifier)
        MessageType.VIDEO -> VideoMessageBubble(message, isOutbound, modifier)
        MessageType.DOCUMENT -> DocumentMessageBubble(
            message = message,
            isOutbound = isOutbound,
            onOpenDoc = onOpenDoc,
            modifier = modifier,
        )
        MessageType.AUDIO -> AudioMessageBubble(message, isOutbound, modifier)
        MessageType.LOCATION -> LocationMessageBubble(
            message = message,
            isOutbound = isOutbound,
            onOpenMaps = onOpenMaps,
            modifier = modifier,
        )
        MessageType.TEMPLATE -> TemplateMessageBubble(
            message = message,
            isOutbound = isOutbound,
            onButtonClick = { onReact(it) },
            modifier = modifier,
        )
    }
}
