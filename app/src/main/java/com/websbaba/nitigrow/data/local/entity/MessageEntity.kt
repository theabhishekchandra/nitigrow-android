package com.websbaba.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("tenantId"),
        Index("conversationId"),
        Index("sentAtEpochMs"),
        Index(value = ["clientId"], unique = false)
    ]
)
data class MessageEntity(
    @PrimaryKey val localId: String,        // clientId for outgoing, serverId for inbound
    // Multi-tenant scope. Defaults to "" so existing mapper call sites that
    // predate tenant-scoping still compile; rows written through the chat
    // repository carry the active tenantId, and the local cache is wiped on
    // logout / tenant switch (see LogoutUseCase) to prevent cross-tenant bleed.
    val tenantId: String = "",
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
