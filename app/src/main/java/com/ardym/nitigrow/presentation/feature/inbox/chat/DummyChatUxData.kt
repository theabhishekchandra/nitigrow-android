package com.ardym.nitigrow.presentation.feature.inbox.chat

import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.domain.model.Template
import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// ─────────────────────────────────────────────────────────────────────────────
// DummyChatUxData
//
// Seeds the Phase-3 Sprint-1B chat UX layer (window banner, template picker,
// reply quote, internal notes) with realistic Indian-SMB content so screens
// render full UI states before the backend is wired.
//
// Every public helper repeats the TODO comment at its call site so a global
// grep for "DummyChatUxData" surfaces every removal point during cutover.
// ─────────────────────────────────────────────────────────────────────────────
object DummyChatUxData {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    /**
     * Helper to compute a fake `windowExpiresAt`. The chat UX layer treats the
     * value as a wall-clock target — banners count down to it once per minute.
     */
    fun windowExpiresIn(hours: Int): Instant =
        Instant.now().plus(hours.toLong(), ChronoUnit.HOURS)

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    /**
     * Sample agent-only internal note for the given conversation. Rendered by
     * `InternalNoteBubble` to demonstrate the yellow-bubble note style.
     */
    fun internalNote(conversationId: String): Message = Message(
        id = "$conversationId-note-1",
        conversationId = conversationId,
        text = "Customer is a returning wholesale buyer — eligible for 8% bulk discount. " +
            "Push the Diwali catalog and follow up in 24h if no reply.",
        sentAt = Instant.now().minus(35, ChronoUnit.MINUTES),
        outbound = true,
        status = MessageStatus.SENT,
        type = MessageType.TEXT
    )

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    /**
     * Two-message exchange demonstrating Reply-to-Specific-Message UX: an
     * inbound `original` and an outbound `reply` whose `replyToMessageId`
     * points at the original. Caller renders the `reply` with `ReplyEcho`
     * embedded above the body.
     */
    fun quotedExchange(conversationId: String): Pair<Message, Message> {
        val original = Message(
            id = "$conversationId-q-original",
            conversationId = conversationId,
            text = "Could you share the wholesale rate for 25kg of basmati rice?",
            sentAt = Instant.now().minus(18, ChronoUnit.MINUTES),
            outbound = false,
            status = MessageStatus.DELIVERED,
            type = MessageType.TEXT
        )
        val reply = Message(
            id = "$conversationId-q-reply",
            conversationId = conversationId,
            text = "₹2,150 for 25kg, ex-warehouse Pune. Delivery to Mumbai ₹220 extra.",
            sentAt = Instant.now().minus(15, ChronoUnit.MINUTES),
            outbound = true,
            status = MessageStatus.READ,
            type = MessageType.TEXT,
            replyToMessageId = original.id
        )
        return original to reply
    }

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    /**
     * Six realistic Indian-SMB WhatsApp templates spanning MARKETING + UTILITY
     * categories, all APPROVED. Variable counts mirror real Meta-template
     * conventions ({{1}}, {{2}}, …).
     */
    fun templates(): List<Template> {
        val now = Instant.now()
        return listOf(
            Template(
                id = "tpl-diwali-2026",
                name = "diwali_offer_2026",
                language = "en_IN",
                category = "MARKETING",
                status = "APPROVED",
                body = "Namaste {{1}}! Wishing you a sparkling Diwali. Flat 20% off on all orders this week. " +
                    "Reply YES to claim or call us on {{2}}.",
                variableCount = 2,
                updatedAt = now.minus(3, ChronoUnit.DAYS)
            ),
            Template(
                id = "tpl-order-confirmation",
                name = "order_confirmation",
                language = "en_IN",
                category = "UTILITY",
                status = "APPROVED",
                body = "Hi {{1}}, your order #{{2}} totalling ₹{{3}} is confirmed. " +
                    "We'll dispatch within 24 hours and share tracking once shipped.",
                variableCount = 3,
                updatedAt = now.minus(7, ChronoUnit.DAYS)
            ),
            Template(
                id = "tpl-payment-reminder",
                name = "payment_reminder",
                language = "en_IN",
                category = "UTILITY",
                status = "APPROVED",
                body = "Hello {{1}}, this is a gentle reminder that invoice #{{2}} of ₹{{3}} is due on {{4}}. " +
                    "Pay via the link below to avoid late fees.",
                variableCount = 4,
                updatedAt = now.minus(10, ChronoUnit.DAYS)
            ),
            Template(
                id = "tpl-appointment-reminder",
                name = "appointment_reminder",
                language = "en_IN",
                category = "UTILITY",
                status = "APPROVED",
                body = "Hi {{1}}, this is a reminder that your appointment with {{2}} is scheduled for " +
                    "{{3}} at {{4}}. Reply CANCEL to reschedule.",
                variableCount = 4,
                updatedAt = now.minus(14, ChronoUnit.DAYS)
            ),
            Template(
                id = "tpl-feedback-request",
                name = "feedback_request",
                language = "en_IN",
                category = "MARKETING",
                status = "APPROVED",
                body = "Thanks for shopping with us, {{1}}! We'd love your feedback on order #{{2}}. " +
                    "Reply with a rating from 1 to 5 — it takes just 5 seconds.",
                variableCount = 2,
                updatedAt = now.minus(21, ChronoUnit.DAYS)
            ),
            Template(
                id = "tpl-cart-abandoned",
                name = "cart_abandoned",
                language = "en_IN",
                category = "MARKETING",
                status = "APPROVED",
                body = "Hey {{1}}, you left {{2}} item(s) in your cart at {{3}}. " +
                    "Complete checkout in the next 6 hours and get 10% off — code BACK10.",
                variableCount = 3,
                updatedAt = now.minus(30, ChronoUnit.DAYS)
            )
        )
    }
}
