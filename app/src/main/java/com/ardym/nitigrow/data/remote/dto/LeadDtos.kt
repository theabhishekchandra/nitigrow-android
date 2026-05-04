package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeadDto(
    @SerializedName("_id") val id: String,
    @SerializedName("contactId") val contactId: String,
    @SerializedName("contactName") val contactName: String,
    @SerializedName("contactPhone") val contactPhone: String,
    @SerializedName("source") val source: String,
    @SerializedName("stage") val stage: String,
    @SerializedName("valueInr") val valueInr: Long,
    @SerializedName("ownerName") val ownerName: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class LeadListResponse(
    @SerializedName("data") val data: List<LeadDto>
)

data class MoveLeadStageRequest(
    @SerializedName("stage") val stage: String
)

data class CreateLeadRequest(
    @SerializedName("contactId") val contactId: String,
    @SerializedName("source") val source: String,
    @SerializedName("stage") val stage: String,
    @SerializedName("valueInr") val valueInr: Long,
    @SerializedName("notes") val notes: String?
)
