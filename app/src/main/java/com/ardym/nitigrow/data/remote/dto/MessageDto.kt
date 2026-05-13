package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("clientId") val clientId: String?,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("text") val text: String,
    @SerializedName("sentAt") val sentAt: String,           // ISO-8601
    @SerializedName("outbound") val outbound: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("type") val type: String,
    @SerializedName("mediaUrl") val mediaUrl: String?,
    @SerializedName("mediaMimeType") val mediaMimeType: String?,
    @SerializedName("mediaSizeBytes") val mediaSizeBytes: Long?,
    @SerializedName("replyToMessageId") val replyToMessageId: String?
)

data class MessagePageResponse(
    @SerializedName("data") val data: List<MessageDto>,
    @SerializedName("nextCursor") val nextCursor: String?
)

data class SendMessageRequest(
    @SerializedName("clientId") val clientId: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String = "TEXT",
    @SerializedName("replyToMessageId") val replyToMessageId: String? = null
)
