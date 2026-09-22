package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.CampaignEntity
import com.websbaba.nitigrow.data.local.entity.TemplateEntity
import com.websbaba.nitigrow.data.remote.dto.CampaignDto
import com.websbaba.nitigrow.data.remote.dto.TemplateDto
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.domain.model.Template
import java.time.Instant

fun CampaignDto.toEntity(): CampaignEntity = CampaignEntity(
    id = id,
    name = name.orEmpty(),
    templateId = templateId.orEmpty(),
    templateName = templateName.orEmpty(),
    audienceTagsCsv = (audienceTags ?: audience?.tags).orEmpty().joinToString("|"),
    audienceSize = audienceSize ?: stats?.total?.toInt() ?: audience?.contactIds?.size ?: 0,
    status = status.orEmpty().uppercase(),
    scheduledAtEpochMs = parseInstantOrNull(scheduledAt),
    sentCount = sentCount ?: stats?.sent ?: 0L,
    deliveredCount = deliveredCount ?: stats?.delivered ?: 0L,
    readCount = readCount ?: stats?.read ?: 0L,
    failedCount = failedCount ?: stats?.failed ?: 0L,
    createdAtEpochMs = createdAt?.let(::parseInstant) ?: System.currentTimeMillis()
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
