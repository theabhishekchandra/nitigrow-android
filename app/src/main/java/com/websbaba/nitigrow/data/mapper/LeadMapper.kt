package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.LeadEntity
import com.websbaba.nitigrow.data.remote.dto.LeadDto
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

fun LeadDto.toEntity(): LeadEntity = LeadEntity(
    id = id,
    contactId = contactId,
    contactName = contactName,
    contactPhone = contactPhone,
    source = source,
    stage = stage.uppercase(),
    valueInr = valueInr,
    ownerName = ownerName,
    notes = notes,
    createdAtEpochMs = parseInstant(createdAt),
    updatedAtEpochMs = parseInstant(updatedAt)
)

fun LeadEntity.toDomain(): Lead = Lead(
    id = id,
    contactId = contactId,
    contactName = contactName,
    contactPhone = contactPhone,
    source = source,
    stage = LeadStage.safeValueOf(stage),
    valueInr = valueInr,
    ownerName = ownerName,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs)
)
