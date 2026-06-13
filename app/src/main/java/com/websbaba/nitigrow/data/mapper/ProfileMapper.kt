package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.ProfileEntity
import com.websbaba.nitigrow.data.local.entity.TeamMemberEntity
import com.websbaba.nitigrow.data.local.entity.TenantEntity
import com.websbaba.nitigrow.data.remote.dto.TeamMemberDto
import com.websbaba.nitigrow.data.remote.dto.TenantDto
import com.websbaba.nitigrow.data.remote.dto.UserDto
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.model.Tenant
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.model.WabaStatus
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

fun UserDto.toProfileEntity(avatarUrl: String? = null) = ProfileEntity(
    id = 0,
    userId = id,
    tenantId = tenantId,
    name = name,
    email = email,
    phone = phone ?: "",
    role = role,
    avatarUrl = avatarUrl
)

fun ProfileEntity.toDomainUser(): User = User(
    id = userId,
    tenantId = tenantId,
    name = name,
    email = email,
    phone = phone,
    role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.AGENT)
)

fun TenantDto.toEntity() = TenantEntity(
    id = 0,
    tenantId = id,
    name = name ?: "",
    wabaPhone = wabaPhone,
    wabaStatus = if (wabaId != null) "ACTIVE" else "NOT_LINKED",
    planName = planName,
    createdAtEpochMs = createdAt?.let { parseInstant(it) } ?: System.currentTimeMillis()
)

fun TenantEntity.toDomain(): Tenant = Tenant(
    id = tenantId,
    name = name,
    wabaPhone = wabaPhone,
    wabaStatus = WabaStatus.safeValueOf(wabaStatus),
    planName = planName,
    createdAtMs = createdAtEpochMs
)

fun TeamMemberDto.toEntity() = TeamMemberEntity(
    id = id,
    name = name,
    email = email,
    role = role.uppercase(),
    isOwner = isOwner,
    joinedAtEpochMs = parseInstant(joinedAt)
)

fun TeamMemberEntity.toDomain(): TeamMember = TeamMember(
    id = id,
    name = name,
    email = email,
    role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.AGENT),
    isOwner = isOwner,
    joinedAtMs = joinedAtEpochMs
)
