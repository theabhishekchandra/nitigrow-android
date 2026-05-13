package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TenantDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("wabaPhone") val wabaPhone: String?,
    @SerializedName("wabaStatus") val wabaStatus: String,
    @SerializedName("planName") val planName: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class TeamMemberDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("isOwner") val isOwner: Boolean,
    @SerializedName("joinedAt") val joinedAt: String
)

data class TeamListResponse(
    @SerializedName("data") val data: List<TeamMemberDto>
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

data class AvatarUploadResponse(
    @SerializedName("avatarUrl") val avatarUrl: String
)

data class NotificationPrefsRequest(
    @SerializedName("chat") val chat: Boolean,
    @SerializedName("campaign") val campaign: Boolean,
    @SerializedName("system") val system: Boolean,
    @SerializedName("sound") val sound: Boolean,
    @SerializedName("preview") val preview: Boolean
)

data class InviteMemberRequest(
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)

data class DeleteAccountRequest(
    @SerializedName("reason") val reason: String?
)
