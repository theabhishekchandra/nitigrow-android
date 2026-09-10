package com.websbaba.nitigrow.domain.model

/** A Meta-native WhatsApp Flow (interactive in-chat form). */
data class WaFlow(
    val id: String,
    val metaFlowId: String?,
    val name: String,
    val categories: List<String>,
    val status: String, // DRAFT | PUBLISHED | DEPRECATED
    val createdAt: String?,
) {
    val isPublished: Boolean get() = status == "PUBLISHED"
}
