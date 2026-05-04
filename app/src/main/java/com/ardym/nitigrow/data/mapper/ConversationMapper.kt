package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.ConversationEntity
import com.ardym.nitigrow.data.remote.dto.ConversationDto
import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.domain.model.MessageStatus
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

private fun statusFromString(value: String): MessageStatus =
    runCatching { MessageStatus.valueOf(value.uppercase()) }
        .getOrDefault(MessageStatus.SENT)

fun ConversationDto.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    contactId = contactId,
    contactName = contactName,
    contactPhone = contactPhone,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    lastMessageAtEpochMs = parseInstant(lastMessageAt),
    lastMessageStatus = lastMessageStatus.uppercase(),
    lastMessageOutbound = lastMessageOutbound,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted
)

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    contactId = contactId,
    contactName = contactName,
    contactPhone = contactPhone,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    lastMessageAt = Instant.ofEpochMilli(lastMessageAtEpochMs),
    lastMessageStatus = statusFromString(lastMessageStatus),
    lastMessageOutbound = lastMessageOutbound,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted
)
