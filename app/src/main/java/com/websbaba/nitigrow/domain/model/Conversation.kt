package com.websbaba.nitigrow.domain.model

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
    val isMuted: Boolean,
    /**
     * When the 24h reply window closes (server-provided). [Instant.EPOCH] means the server
     * reports no open window; null means unknown (older cache), so callers fall back to
     * deriving it from the last inbound message.
     */
    val windowExpiresAt: Instant? = null
) {
    /** False for a contact who has never exchanged a message ([lastMessageAt] is epoch-zero). */
    val hasMessages: Boolean get() = lastMessageAt != Instant.EPOCH
}

enum class MessageStatus { PENDING, SENT, DELIVERED, READ, FAILED }
