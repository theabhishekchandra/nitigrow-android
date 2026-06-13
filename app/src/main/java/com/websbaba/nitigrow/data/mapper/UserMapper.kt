package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.remote.dto.UserDto
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.model.UserRole

fun UserDto.toDomain(): User = User(
    id = id,
    tenantId = tenantId,
    name = name,
    email = email,
    phone = phone ?: "",
    role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.AGENT)
)
