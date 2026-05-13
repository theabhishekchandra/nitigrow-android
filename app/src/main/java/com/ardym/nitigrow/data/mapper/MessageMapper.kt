package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.MessageEntity
import com.ardym.nitigrow.data.remote.dto.MessageDto
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstantSafe(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

private fun statusFromString(value: String): MessageStatus =
    runCatching { MessageStatus.valueOf(value.uppercase()) }
        .getOrDefault(MessageStatus.SENT)

private fun typeFromString(value: String): MessageType =
    runCatching { MessageType.valueOf(value.uppercase()) }
        .getOrDefault(MessageType.TEXT)

fun MessageDto.toEntity(): MessageEntity = MessageEntity(
    localId = id,
    serverId = id,
    clientId = clientId,
    conversationId = conversationId,
    text = text,
    sentAtEpochMs = parseInstantSafe(sentAt),
    outbound = outbound,
    status = status.uppercase(),
    type = type.uppercase(),
    mediaUrl = mediaUrl,
    mediaMimeType = mediaMimeType,
    mediaSizeBytes = mediaSizeBytes,
    replyToMessageId = replyToMessageId,
    errorReason = null
)

fun MessageEntity.toDomain(): Message = Message(
    id = localId,
    conversationId = conversationId,
    text = text,
    sentAt = Instant.ofEpochMilli(sentAtEpochMs),
    outbound = outbound,
    status = statusFromString(status),
    type = typeFromString(type),
    mediaUrl = mediaUrl,
    mediaMimeType = mediaMimeType,
    mediaSizeBytes = mediaSizeBytes,
    replyToMessageId = replyToMessageId,
    errorReason = errorReason
)
