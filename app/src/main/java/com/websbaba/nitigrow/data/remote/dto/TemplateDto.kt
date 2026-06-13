package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

// Full WhatsApp template doc from GET /api/templates (distinct from the flattened
// TemplateDto used by the campaign picker).
data class WaTemplateDto(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val category: String = "MARKETING",
    val language: String = "en",
    val status: String = "PENDING",
    val components: List<TemplateComponentDto> = emptyList(),
    val rejectionReason: String? = null,
    val updatedAt: String? = null
)

data class TemplateComponentDto(
    val type: String? = null,
    val format: String? = null,
    val text: String? = null
)

data class CreateTemplateRequest(
    val name: String,
    val category: String,
    val language: String,
    val components: List<CreateTemplateComponent>
)

data class CreateTemplateComponent(
    val type: String,
    val format: String,
    val text: String
)
