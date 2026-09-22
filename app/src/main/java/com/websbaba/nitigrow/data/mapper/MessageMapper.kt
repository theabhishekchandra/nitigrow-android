package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.MessageEntity
import com.websbaba.nitigrow.data.remote.dto.MessageDto
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageType
import java.time.Instant

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
    sentAtEpochMs = parseInstant(createdAt),
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
    status = messageStatusOf(status),
    type = typeFromString(type),
    mediaUrl = mediaUrl,
    mediaMimeType = mediaMimeType,
    mediaSizeBytes = mediaSizeBytes,
    replyToMessageId = replyToMessageId,
    errorReason = errorReason
)
