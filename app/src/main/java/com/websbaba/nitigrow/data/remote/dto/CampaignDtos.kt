package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * A campaign as the backend sends it. The API nests the audience and the send
 * counters (`audience.tags`, `stats.sent` …); older payloads carried them flat
 * (`audienceTags`, `sentCount` …). Both shapes are read, and every field the
 * server might omit is nullable — Gson does not enforce Kotlin nullability, so
 * a missing "non-null" field would otherwise crash the mapper.
 */
data class CampaignDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("templateId") val templateId: String?,
    @SerializedName("templateName") val templateName: String?,
    @SerializedName("audience") val audience: CampaignAudienceDto? = null,
    @SerializedName("stats") val stats: CampaignStatsDto? = null,
    @SerializedName("audienceTags") val audienceTags: List<String>? = null,
    @SerializedName("audienceSize") val audienceSize: Int? = null,
    @SerializedName("status") val status: String?,
    @SerializedName("scheduledAt") val scheduledAt: String? = null,
    @SerializedName("sentCount") val sentCount: Long? = null,
    @SerializedName("deliveredCount") val deliveredCount: Long? = null,
    @SerializedName("readCount") val readCount: Long? = null,
    @SerializedName("failedCount") val failedCount: Long? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class CampaignAudienceDto(
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("contactIds") val contactIds: List<String>? = null
)

data class CampaignStatsDto(
    @SerializedName("total") val total: Long? = null,
    @SerializedName("sent") val sent: Long? = null,
    @SerializedName("delivered") val delivered: Long? = null,
    @SerializedName("read") val read: Long? = null,
    @SerializedName("failed") val failed: Long? = null
)

// Backend returns { campaigns: [...], total, page, pages }.
data class CampaignListResponse(
    @SerializedName("campaigns") val data: List<CampaignDto>? = null
)

/**
 * POST /api/campaigns body. The backend's Joi schema (backend/src/middleware/validate.js
 * createCampaignSchema) requires a nested `audience` object and strips any other top-level
 * key — a flat `audienceTags` field was silently dropped, so every campaign created from
 * this app defaulted to `audience: { type: 'all' }` and blasted the entire contact list
 * regardless of the tags the user picked.
 */
data class CreateCampaignRequest(
    @SerializedName("name") val name: String,
    @SerializedName("templateId") val templateId: String,
    @SerializedName("audience") val audience: CampaignAudienceRequest,
    @SerializedName("scheduledAt") val scheduledAt: String?
)

data class CampaignAudienceRequest(
    @SerializedName("type") val type: String,
    @SerializedName("tags") val tags: List<String>? = null
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
    // Both closing braces are escaped on purpose: Android's ICU regex engine rejects a
    // bare "}" (PatternSyntaxException) even though the desktop JVM accepts it.
    val variableCount: Int
        get() = VARIABLE_PATTERN.findAll(body).map { it.groupValues[1] }.distinct().count()
}

data class AudienceEstimateRequest(
    @SerializedName("tags") val tags: List<String>
)

data class AudienceEstimateResponse(
    @SerializedName("count") val count: Int
)

/** Matches `{{1}}`-style template placeholders. Compiled once; braces escaped for Android (see above). */
private val VARIABLE_PATTERN = Regex("""\{\{\s*(\d+)\s*\}\}""")
