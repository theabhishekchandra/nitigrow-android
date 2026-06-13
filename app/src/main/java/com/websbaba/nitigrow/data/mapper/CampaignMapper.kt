package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.CampaignEntity
import com.websbaba.nitigrow.data.local.entity.TemplateEntity
import com.websbaba.nitigrow.data.remote.dto.CampaignDto
import com.websbaba.nitigrow.data.remote.dto.TemplateDto
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.domain.model.Template
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstantOpt(iso: String?): Long? = iso?.let {
    try { Instant.parse(it).toEpochMilli() } catch (_: DateTimeParseException) { null }
}

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

fun CampaignDto.toEntity(): CampaignEntity = CampaignEntity(
    id = id,
    name = name,
    templateId = templateId,
    templateName = templateName,
    audienceTagsCsv = audienceTags.joinToString("|"),
    audienceSize = audienceSize,
    status = status.uppercase(),
    scheduledAtEpochMs = parseInstantOpt(scheduledAt),
    sentCount = sentCount,
    deliveredCount = deliveredCount,
    readCount = readCount,
    failedCount = failedCount,
    createdAtEpochMs = parseInstant(createdAt)
)

fun CampaignEntity.toDomain(): Campaign = Campaign(
    id = id,
    name = name,
    templateId = templateId,
    templateName = templateName,
    audienceTags = if (audienceTagsCsv.isBlank()) emptyList()
                   else audienceTagsCsv.split('|').filter { it.isNotBlank() },
    audienceSize = audienceSize,
    status = CampaignStatus.safeValueOf(status),
    scheduledAt = scheduledAtEpochMs?.let(Instant::ofEpochMilli),
    sentCount = sentCount,
    deliveredCount = deliveredCount,
    readCount = readCount,
    failedCount = failedCount,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs)
)

fun TemplateDto.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    language = language,
    category = category,
    status = status,
    body = body,
    variableCount = variableCount,
    updatedAtEpochMs = parseInstant(updatedAt)
)

fun TemplateEntity.toDomain(): Template = Template(
    id = id,
    name = name,
    language = language,
    category = category,
    status = status,
    body = body,
    variableCount = variableCount,
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs)
)
