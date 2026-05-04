package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.ContactEntity
import com.ardym.nitigrow.data.remote.dto.ContactDto
import com.ardym.nitigrow.domain.model.Contact
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

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
