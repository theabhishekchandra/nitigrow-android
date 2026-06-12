package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.remote.dto.UserDto
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.model.UserRole

fun UserDto.toDomain(): User = User(
    id = id,
    tenantId = tenantId,
    name = name,
    email = email,
    phone = phone ?: "",
    role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.AGENT)
)
