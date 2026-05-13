// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
package com.ardym.nitigrow.presentation.feature.templates

import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Seeded templates that mirror the typical content an Indian SMB ships through Meta
// (Marketing offers, Utility order/payment notifications, Authentication OTPs).
// Status mix is intentional — 6 APPROVED / 3 PENDING / 2 REJECTED so the filter
// chips on the list screen all have something to show.
object DummyTemplatesData {

    private fun hoursAgo(h: Long): Instant = Instant.now().minus(h, ChronoUnit.HOURS)
    private fun daysAgo(d: Long): Instant = Instant.now().minus(d, ChronoUnit.DAYS)

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun all(): List<Template> = listOf(
        Template(
            id = "tpl-001",
            name = "diwali_offer_2026",
            language = TemplateLanguage.EN,
            category = TemplateCategory.MARKETING,
            status = TemplateStatus.APPROVED,
            body = "Hi {{1}}, celebrate Diwali with NitiGrow! Get flat 25% off on all orders above ₹2,000. Use code DIWALI25. Valid till 12 Nov. Reply STOP to opt out.",
            updatedAt = hoursAgo(6),
        ),
        Template(
            id = "tpl-002",
            name = "order_confirmation",
            language = TemplateLanguage.EN,
            category = TemplateCategory.UTILITY,
            status = TemplateStatus.APPROVED,
            body = "Hello {{1}}, your order #{{2}} has been confirmed. Total: ₹{{3}}. Expected delivery by {{4}}. Track here: {{5}}",
            updatedAt = hoursAgo(18),
        ),
        Template(
            id = "tpl-003",
            name = "payment_received",
            language = TemplateLanguage.EN,
            category = TemplateCategory.UTILITY,
            status = TemplateStatus.APPROVED,
            body = "Hi {{1}}, we have received your payment of ₹{{2}} for invoice #{{3}}. Thank you for choosing us! 🙏",
            updatedAt = daysAgo(1),
        ),
        Template(
            id = "tpl-004",
            name = "appointment_reminder",
            language = TemplateLanguage.EN,
            category = TemplateCategory.UTILITY,
            status = TemplateStatus.APPROVED,
            body = "Hi {{1}}, this is a reminder for your appointment on {{2}} at {{3}}. Please reply CONFIRM or RESCHEDULE.",
            updatedAt = daysAgo(2),
        ),
        Template(
            id = "tpl-005",
            name = "welcome_message",
            language = TemplateLanguage.EN,
            category = TemplateCategory.MARKETING,
            status = TemplateStatus.APPROVED,
            body = "Welcome to {{1}}, {{2}}! We're delighted to have you on board. Browse our latest collection here: {{3}}",
            updatedAt = daysAgo(3),
        ),
        Template(
            id = "tpl-006",
            name = "holi_special",
            language = TemplateLanguage.HI,
            category = TemplateCategory.MARKETING,
            status = TemplateStatus.APPROVED,
            body = "नमस्ते {{1}}, होली के रंगों के साथ पाएं 20% की छूट। कूपन कोड: HOLI20। ऑफर {{2}} तक मान्य है।",
            updatedAt = daysAgo(5),
        ),
        Template(
            id = "tpl-007",
            name = "cart_abandoned",
            language = TemplateLanguage.EN,
            category = TemplateCategory.MARKETING,
            status = TemplateStatus.PENDING,
            body = "Hi {{1}}, you left ₹{{2}} worth of items in your cart. Complete your order now and get free delivery. Shop: {{3}}",
            updatedAt = hoursAgo(3),
        ),
        Template(
            id = "tpl-008",
            name = "feedback_request",
            language = TemplateLanguage.EN,
            category = TemplateCategory.UTILITY,
            status = TemplateStatus.PENDING,
            body = "Hello {{1}}, how was your recent experience with order #{{2}}? Please rate us 1-5: {{3}}. Your feedback helps us improve.",
            updatedAt = hoursAgo(9),
        ),
        Template(
            id = "tpl-009",
            name = "otp_verification",
            language = TemplateLanguage.EN,
            category = TemplateCategory.AUTHENTICATION,
            status = TemplateStatus.PENDING,
            body = "Your NitiGrow verification code is {{1}}. This code expires in 10 minutes. Do not share it with anyone.",
            updatedAt = hoursAgo(14),
        ),
        Template(
            id = "tpl-010",
            name = "birthday_wish",
            language = TemplateLanguage.HI,
            category = TemplateCategory.MARKETING,
            status = TemplateStatus.REJECTED,
            rejectionReason = "Promotional content not aligned with template category. Resubmit under MARKETING with opt-out link.",
            body = "जन्मदिन मुबारक हो {{1}}! आज के दिन हमारी ओर से आपको खास तोहफा — फ्लैट 30% की छूट। कोड: BDAY30।",
            updatedAt = daysAgo(4),
        ),
        Template(
            id = "tpl-011",
            name = "gst_invoice",
            language = TemplateLanguage.EN,
            category = TemplateCategory.UTILITY,
            status = TemplateStatus.REJECTED,
            rejectionReason = "Missing required variable for GSTIN. Please add {{4}} for tax registration number.",
            body = "Hi {{1}}, your GST invoice for order #{{2}} totalling ₹{{3}} is attached. Download: {{4}}",
            updatedAt = daysAgo(6),
        ),
    )
}
