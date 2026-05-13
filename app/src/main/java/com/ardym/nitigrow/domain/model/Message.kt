package com.ardym.nitigrow.domain.model

import java.time.Instant

data class Message(
    val id: String,                 // local clientId or server id
    val conversationId: String,
    val text: String,
    val sentAt: Instant,
    val outbound: Boolean,
    val status: MessageStatus,
    val type: MessageType,
    val mediaUrl: String? = null,
    val mediaMimeType: String? = null,
    val mediaSizeBytes: Long? = null,
    val replyToMessageId: String? = null,
    val errorReason: String? = null
)

enum class MessageType { TEXT, IMAGE, VIDEO, DOCUMENT, AUDIO, LOCATION, TEMPLATE }
