package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.ContactEntity
import com.websbaba.nitigrow.data.remote.dto.ContactDto
import com.websbaba.nitigrow.domain.model.Contact
import java.time.Instant

fun ContactDto.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    nameKey = name.trim().lowercase(),
    phone = phone,
    email = email,
    avatarUrl = avatarUrl,
    tagsCsv = (tags ?: emptyList()).joinToString("|"),
    notes = notes,
    createdAtEpochMs = parseInstant(createdAt),
    updatedAtEpochMs = parseInstant(updatedAt),
    isBlocked = isBlocked
)

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phone = phone,
    email = email,
    avatarUrl = avatarUrl,
    tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split('|').filter { it.isNotBlank() },
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs),
    isBlocked = isBlocked
)
