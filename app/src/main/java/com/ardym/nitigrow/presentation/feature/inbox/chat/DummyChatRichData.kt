// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.

package com.ardym.nitigrow.presentation.feature.inbox.chat

import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import java.time.Instant

/**
 * Factory of sample [Message] instances — one per rich message subtype — used
 * for Compose previews and developer-only mock screens. Every factory carries
 * the deletion banner because none of this content should reach production.
 */
object DummyChatRichData {

    private const val CONV_ID = "demo-conv"

    private fun base(
        id: String,
        text: String,
        type: MessageType,
        outbound: Boolean,
        status: MessageStatus = MessageStatus.DELIVERED,
        mediaUrl: String? = null,
        mediaMimeType: String? = null,
        mediaSizeBytes: Long? = null,
    ) = Message(
        id = id,
        conversationId = CONV_ID,
        text = text,
        sentAt = Instant.now(),
        outbound = outbound,
        status = status,
        type = type,
        mediaUrl = mediaUrl,
        mediaMimeType = mediaMimeType,
        mediaSizeBytes = mediaSizeBytes,
    )

    fun image(outbound: Boolean = true): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-img",
            text = "Here's the brochure you asked for",
            type = MessageType.IMAGE,
            outbound = outbound,
            mediaUrl = "https://picsum.photos/seed/niti-img/600/400",
            mediaMimeType = "image/jpeg",
            mediaSizeBytes = 312_456,
        )
    }

    fun video(outbound: Boolean = false): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-vid",
            text = "Product walkthrough",
            type = MessageType.VIDEO,
            outbound = outbound,
            mediaUrl = "https://picsum.photos/seed/niti-vid/600/400",
            mediaMimeType = "video/mp4",
            mediaSizeBytes = 4_812_000,
        )
    }

    fun document(outbound: Boolean = true): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-doc",
            text = "Invoice-2026-001.pdf",
            type = MessageType.DOCUMENT,
            outbound = outbound,
            mediaUrl = "https://example.com/Invoice-2026-001.pdf",
            mediaMimeType = "application/pdf",
            mediaSizeBytes = 248_320,
        )
    }

    fun audio(outbound: Boolean = false): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-audio",
            text = "",
            type = MessageType.AUDIO,
            outbound = outbound,
            mediaUrl = "https://example.com/voice-note.ogg",
            mediaMimeType = "audio/ogg",
            mediaSizeBytes = 192_000,
        )
    }

    fun location(outbound: Boolean = false): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-loc",
            text = "19.0760, 72.8777",
            type = MessageType.LOCATION,
            outbound = outbound,
        )
    }

    fun sticker(outbound: Boolean = true): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        // Encoded as TEXT + "sticker://" prefix per the dispatcher convention
        // (MessageType enum has no STICKER member yet).
        return base(
            id = "dummy-sticker",
            text = "🎉",
            type = MessageType.TEXT,
            outbound = outbound,
        )
    }

    fun template(outbound: Boolean = true): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        return base(
            id = "dummy-tpl",
            text = "Order Confirmed\n\nThanks for your order #1024. Your package will arrive by tomorrow.\n[btn]Track order\n[btn]Contact support",
            type = MessageType.TEMPLATE,
            outbound = outbound,
            mediaUrl = "https://picsum.photos/seed/niti-tpl/600/300",
            mediaMimeType = "image/jpeg",
        )
    }

    fun carousel(outbound: Boolean = true): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        // Encoded as TEMPLATE + "|||" delimiter per the dispatcher convention.
        return base(
            id = "dummy-carousel",
            text = "https://picsum.photos/seed/c1/300/200::Premium Plan::Unlimited messages, 5 agents::Choose plan" +
                "|||https://picsum.photos/seed/c2/300/200::Growth Plan::10k messages, 2 agents::Choose plan" +
                "|||https://picsum.photos/seed/c3/300/200::Starter Plan::1k messages, 1 agent::Choose plan",
            type = MessageType.TEMPLATE,
            outbound = outbound,
        )
    }

    fun reaction(outbound: Boolean = false): Message {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        // Encoded as TEXT + special mediaMimeType "reaction/emoji" per dispatcher convention.
        return base(
            id = "dummy-reaction",
            text = "❤️",
            type = MessageType.TEXT,
            outbound = outbound,
            mediaMimeType = "reaction/emoji",
        )
    }

    fun all(): List<Message> = listOf(
        image(), video(), document(), audio(),
        location(), sticker(), template(), carousel(), reaction(),
    ).sortedByDescending { it.sentAt }
}
