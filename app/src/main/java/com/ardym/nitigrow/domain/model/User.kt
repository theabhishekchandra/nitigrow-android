package com.ardym.nitigrow.domain.model

/**
 * Domain model. No serialization annotations, no Room annotations.
 * DTO/Entity get mapped to this in `data/mapper/`.
 */
data class User(
    val id: String,
    val tenantId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole
)

enum class UserRole { OWNER, ADMIN, AGENT }
