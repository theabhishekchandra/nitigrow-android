package com.websbaba.nitigrow.domain.model

/** A standalone payment link sent to a contact (Phase 3 mobile "Send payment link"). */
data class PaymentLink(
    val id: String,
    val contactName: String,
    val amount: Long,       // rupees
    val status: String,     // created | captured | failed | refunded
    val linkUrl: String?,
    val sentAt: String?      // ISO-8601
)
