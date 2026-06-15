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

data class TemplateDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("language") val language: String,
    @SerializedName("category") val category: String,
    @SerializedName("status") val status: String,
    @SerializedName("body") val body: String,
    @SerializedName("variableCount") val variableCount: Int,
    @SerializedName("updatedAt") val updatedAt: String
)

data class TemplateListResponse(
    @SerializedName("data") val data: List<TemplateDto>? = null
)

data class AudienceEstimateRequest(
    @SerializedName("tags") val tags: List<String>
)

data class AudienceEstimateResponse(
    @SerializedName("count") val count: Int
)
