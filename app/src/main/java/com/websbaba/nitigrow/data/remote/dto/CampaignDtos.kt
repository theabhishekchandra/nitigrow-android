package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CampaignDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("templateId") val templateId: String,
    @SerializedName("templateName") val templateName: String,
    @SerializedName("audienceTags") val audienceTags: List<String>,
    @SerializedName("audienceSize") val audienceSize: Int,
    @SerializedName("status") val status: String,
    @SerializedName("scheduledAt") val scheduledAt: String?,
    @SerializedName("sentCount") val sentCount: Long,
    @SerializedName("deliveredCount") val deliveredCount: Long,
    @SerializedName("readCount") val readCount: Long,
    @SerializedName("failedCount") val failedCount: Long,
    @SerializedName("createdAt") val createdAt: String
)

// Backend returns { campaigns: [...], total, page, pages }.
data class CampaignListResponse(
    @SerializedName("campaigns") val data: List<CampaignDto>? = null
)

data class CreateCampaignRequest(
    @SerializedName("name") val name: String,
    @SerializedName("templateId") val templateId: String,
    @SerializedName("audienceTags") val audienceTags: List<String>,
    @SerializedName("scheduledAt") val scheduledAt: String?
)

// GET /templates returns a TOP-LEVEL array of these. The backend sends
// `components`, not a flat `body`/`variableCount`, so the picker derives both
// from the BODY component (keeps TemplateDto.toEntity unchanged).
data class TemplateDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("language") val language: String = "en",
    @SerializedName("category") val category: String = "MARKETING",
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("components") val components: List<TemplateComponentDto> = emptyList(),
    @SerializedName("updatedAt") val updatedAt: String = ""
) {
    val body: String
        get() = components.firstOrNull { it.type.equals("BODY", ignoreCase = true) }?.text.orEmpty()
    val variableCount: Int
        get() = Regex("""\{\{\s*(\d+)\s*}}""").findAll(body).map { it.groupValues[1] }.distinct().count()
}

data class AudienceEstimateRequest(
    @SerializedName("tags") val tags: List<String>
)

data class AudienceEstimateResponse(
    @SerializedName("count") val count: Int
)
