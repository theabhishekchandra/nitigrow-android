package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class WaFlowDto(
    @SerializedName("_id") val id: String,
    @SerializedName("metaFlowId") val metaFlowId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("status") val status: String = "DRAFT",
    @SerializedName("createdAt") val createdAt: String? = null,
)
data class WaFlowListResponse(@SerializedName("data") val data: List<WaFlowDto>? = null)

data class SendFlowRequest(
    @SerializedName("to") val to: String,
    @SerializedName("cta") val cta: String? = null,
    @SerializedName("bodyText") val bodyText: String? = null,
    @SerializedName("headerText") val headerText: String? = null,
)
data class SendFlowResultDto(@SerializedName("data") val data: JsonElement? = null)

data class FlowContactDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
)
data class FlowSubmissionDto(
    @SerializedName("_id") val id: String,
    @SerializedName("flowName") val flowName: String? = null,
    @SerializedName("contact") val contact: FlowContactDto? = null,
    @SerializedName("responseJson") val responseJson: JsonObject? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)
data class FlowSubmissionListResponse(@SerializedName("data") val data: List<FlowSubmissionDto>? = null)
