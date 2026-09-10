package com.websbaba.nitigrow.domain.model

/** A completed WhatsApp Flow — the field values a customer submitted. */
data class FlowSubmission(
    val id: String,
    val flowName: String?,
    val contactName: String?,
    val contactPhone: String?,
    val fields: Map<String, String>,
    val createdAt: String?,
)
