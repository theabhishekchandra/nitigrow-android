package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TenantDto(
    @SerializedName("_id") val id: String,
    @SerializedName("businessName") val name: String?,
    @SerializedName("displayPhoneNumber") val wabaPhone: String?,
    @SerializedName("wabaId") val wabaId: String?,
    @SerializedName("plan") val planName: String?,
    @SerializedName("createdAt") val createdAt: String?
)

// The backend sends a bare array and no `isOwner`/`joinedAt` — the join date is the
// user's `createdAt` — so everything but the id is optional.
data class TeamMemberDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("isOwner") val isOwner: Boolean? = null,
    @SerializedName(value = "joinedAt", alternate = ["createdAt"]) val joinedAt: String? = null
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

/**
 * POST /api/team/invite body. The backend (backend/src/controllers/teamController.js)
 * requires `name` — it emails the invite and has nothing else to address it by — and
 * validates `role` against its own vocabulary (owner/manager/sales_agent/support_agent/
 * campaign_manager/analyst/accountant), not "ADMIN"/"AGENT".
 */
data class InviteMemberRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)

/** The invite response nests the created user under `user` — never a bare TeamMemberDto. */
data class InviteMemberResponse(
    @SerializedName("user") val user: TeamMemberDto
)

data class DeleteAccountRequest(
    @SerializedName("password") val password: String
)
