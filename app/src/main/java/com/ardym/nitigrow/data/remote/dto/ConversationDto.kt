package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("_id") val id: String,
    @SerializedName("contactId") val contactId: String,
    @SerializedName("contactName") val contactName: String,
    @SerializedName("contactPhone") val contactPhone: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastMessageAt") val lastMessageAt: String, // ISO-8601
    @SerializedName("lastMessageStatus") val lastMessageStatus: String,
    @SerializedName("lastMessageOutbound") val lastMessageOutbound: Boolean,
    @SerializedName("unreadCount") val unreadCount: Int,
    @SerializedName("isPinned") val isPinned: Boolean,
    @SerializedName("isMuted") val isMuted: Boolean
)

data class ConversationListResponse(
    @SerializedName("data") val data: List<ConversationDto>,
    @SerializedName("nextCursor") val nextCursor: String?
)

data class TogglePinRequest(
    @SerializedName("pinned") val pinned: Boolean
)
