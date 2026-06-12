package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.ProfileEntity
import com.ardym.nitigrow.data.local.entity.TeamMemberEntity
import com.ardym.nitigrow.data.local.entity.TenantEntity
import com.ardym.nitigrow.data.remote.dto.TeamMemberDto
import com.ardym.nitigrow.data.remote.dto.TenantDto
import com.ardym.nitigrow.data.remote.dto.UserDto
import com.ardym.nitigrow.domain.model.TeamMember
import com.ardym.nitigrow.domain.model.Tenant
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.model.UserRole
import com.ardym.nitigrow.domain.model.WabaStatus
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
    name = name,
    wabaPhone = wabaPhone,
    wabaStatus = wabaStatus.uppercase(),
    planName = planName,
    createdAtEpochMs = parseInstant(createdAt)
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
