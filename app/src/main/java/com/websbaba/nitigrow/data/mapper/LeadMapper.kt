package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.LeadEntity
import com.websbaba.nitigrow.data.remote.dto.LeadDto
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import java.time.Instant

fun LeadDto.toEntity(): LeadEntity {
    val created = createdAt?.let(::parseInstant) ?: System.currentTimeMillis()
    return LeadEntity(
        id = id,
        contactId = contactId.orEmpty(),
        contactName = (name ?: contactName).orEmpty(),
        contactPhone = (phone ?: contactPhone).orEmpty(),
        source = source.orEmpty(),
        stage = LeadStage.fromBackend(stage).name,
        valueInr = value ?: valueInr ?: 0L,
        ownerName = assignedTo?.name ?: ownerName,
        notes = notes,
        createdAtEpochMs = created,
        updatedAtEpochMs = updatedAt?.let(::parseInstant) ?: created
    )
}

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
