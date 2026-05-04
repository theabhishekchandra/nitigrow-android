package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.LeadEntity
import com.ardym.nitigrow.data.remote.dto.LeadDto
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
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
