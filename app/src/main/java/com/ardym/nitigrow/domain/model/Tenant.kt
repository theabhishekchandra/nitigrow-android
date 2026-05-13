package com.ardym.nitigrow.domain.model

data class Tenant(
    val id: String,
    val name: String,
    val wabaPhone: String?,
    val wabaStatus: WabaStatus,
    val planName: String?,
    val createdAtMs: Long
)

enum class WabaStatus {
    NOT_LINKED, PENDING, ACTIVE, SUSPENDED, FAILED;

    companion object {
        fun safeValueOf(raw: String) =
            runCatching { valueOf(raw.uppercase()) }.getOrDefault(NOT_LINKED)
    }
}

data class TeamMember(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val isOwner: Boolean,
    val joinedAtMs: Long
)

data class NotificationPreferences(
    val chatEnabled: Boolean = true,
    val campaignEnabled: Boolean = true,
    val systemEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val previewVisible: Boolean = true
)
