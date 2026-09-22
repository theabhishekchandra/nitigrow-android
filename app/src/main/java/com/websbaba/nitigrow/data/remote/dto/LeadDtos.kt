package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * A lead as the backend sends it. The API uses `name`, `phone`, `value` and
 * `assignedTo { name }`; the fields this app first modelled (`contactName`,
 * `contactPhone`, `valueInr`, `ownerName`) are still read as a fallback. Every
 * field the server might omit is nullable — Gson ignores Kotlin nullability, so a
 * missing "non-null" field would otherwise crash the mapper.
 */
data class LeadDto(
    @SerializedName("_id") val id: String,
    @SerializedName("contactId") val contactId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("contactName") val contactName: String? = null,
    @SerializedName("contactPhone") val contactPhone: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("stage") val stage: String? = null,
    @SerializedName("value") val value: Long? = null,
    @SerializedName("valueInr") val valueInr: Long? = null,
    @SerializedName("assignedTo") val assignedTo: LeadOwnerDto? = null,
    @SerializedName("ownerName") val ownerName: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class LeadOwnerDto(
    @SerializedName("name") val name: String? = null
)

data class LeadListResponse(
    @SerializedName("data") val data: List<LeadDto>? = null
)

data class MoveLeadStageRequest(
    @SerializedName("stage") val stage: String
)

/**
 * POST /api/leads body. The backend model (backend/src/models/Lead.js) requires
 * `name` and stores the deal size as `value`, not `valueInr` — it does not look
 * the contact up by [contactId] to backfill a display name.
 */
data class CreateLeadRequest(
    @SerializedName("contactId") val contactId: String,
    @SerializedName("name") val name: String,
    @SerializedName("source") val source: String,
    @SerializedName("stage") val stage: String,
    @SerializedName("value") val valueInr: Long,
    @SerializedName("notes") val notes: String?
)
