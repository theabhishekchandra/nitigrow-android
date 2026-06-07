package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.MessageEntity
import com.ardym.nitigrow.data.remote.dto.MessageDto
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstantSafe(iso: String?): Long =
    if (iso.isNullOrBlank()) System.currentTimeMillis()
    else try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

private fun statusFromString(value: String): MessageStatus =
    runCatching { MessageStatus.valueOf(value.uppercase()) }
        .getOrDefault(MessageStatus.SENT)

private fun typeFromString(value: String): MessageType =
    runCatching { MessageType.valueOf(value.uppercase()) }
        .getOrDefault(MessageType.TEXT)

/**
 * Maps a backend Mongo Message into the local entity.
 *
 * The thread is keyed by `contactId` (the app's conversation id). Server
 * messages have no client id, so [MessageEntity.clientId] is null and the
 * Mongo `_id` is used for both `localId` and `serverId`. The polymorphic
 * `content` is already flattened to text by [MessageDto.text]; `direction` is
 * normalised to the boolean [MessageDto.outbound]; `status`/`type` are
 * upper-cased to match the entity's string-enum convention.
 */
fun MessageDto.toEntity(): MessageEntity = MessageEntity(
    localId = id,
    serverId = id,
    clientId = null,
    conversationId = contactId,
    text = text,
    sentAtEpochMs = parseInstantSafe(createdAt),
    outbound = outbound,
    status = status.uppercase(),
    type = type.uppercase(),
    mediaUrl = mediaUrl,
    mediaMimeType = null,
    mediaSizeBytes = null,
    replyToMessageId = null,
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
