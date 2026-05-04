package com.ardym.nitigrow.domain.model

import java.time.Instant

data class Conversation(
    val id: String,
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val lastMessageStatus: MessageStatus,
    val lastMessageOutbound: Boolean,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean
)

enum class MessageStatus { PENDING, SENT, DELIVERED, READ, FAILED }
