package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.ConversationEntity
import com.websbaba.nitigrow.data.remote.dto.ConversationDto
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.model.MessageStatus
import java.time.Instant

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
    isMuted = isMuted,
    windowExpiresAtEpochMs = windowExpiresAt?.let(::parseInstant) ?: 0L
)

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    contactId = contactId,
    contactName = contactName,
    contactPhone = contactPhone,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    lastMessageAt = Instant.ofEpochMilli(lastMessageAtEpochMs),
    lastMessageStatus = messageStatusOf(lastMessageStatus),
    lastMessageOutbound = lastMessageOutbound,
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted,
    windowExpiresAt = when (windowExpiresAtEpochMs) {
        ConversationEntity.WINDOW_UNKNOWN -> null
        else -> Instant.ofEpochMilli(windowExpiresAtEpochMs)
    }
)
