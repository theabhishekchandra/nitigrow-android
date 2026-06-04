package com.ardym.nitigrow.domain.model

/**
 * A WhatsApp message template as returned by the backend (/api/templates).
 * Strings (not enums) at the domain boundary; the templates feature maps these
 * to its local UI enums.
 */
data class MessageTemplate(
    val id: String,
    val name: String,
    val category: String,   // MARKETING | UTILITY | AUTHENTICATION
    val language: String,   // en | hi | mr | ...
    val status: String,     // PENDING | APPROVED | REJECTED | PAUSED
    val body: String,       // text of the BODY component
    val rejectionReason: String?,
    val updatedAt: String?   // ISO-8601; UI parses
)
