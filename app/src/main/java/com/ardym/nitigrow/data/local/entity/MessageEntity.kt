package com.ardym.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("conversationId"),
        Index("sentAtEpochMs"),
        Index(value = ["clientId"], unique = false)
    ]
)
data class MessageEntity(
    @PrimaryKey val localId: String,        // clientId for outgoing, serverId for inbound
    val serverId: String?,                  // null until server confirms
    val clientId: String?,                  // populated for outgoing only
    val conversationId: String,
    val text: String,
    val sentAtEpochMs: Long,
    val outbound: Boolean,
    val status: String,
    val type: String,
    val mediaUrl: String?,
    val mediaMimeType: String?,
    val mediaSizeBytes: Long?,
    val replyToMessageId: String?,
    val errorReason: String?
)
