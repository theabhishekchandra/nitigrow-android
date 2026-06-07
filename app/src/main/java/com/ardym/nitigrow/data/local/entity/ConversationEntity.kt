package com.ardym.nitigrow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index("tenantId"),
        Index("lastMessageAtEpochMs"),
        Index("isPinned"),
        Index("contactName")
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    // Multi-tenant scope. Defaults to "" so existing mapper call sites that
    // predate tenant-scoping still compile; the local cache is wiped on
    // logout / tenant switch (see LogoutUseCase) to prevent cross-tenant bleed.
    val tenantId: String = "",
    val contactId: String,
    val contactName: String,
    val contactPhone: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageAtEpochMs: Long,
    val lastMessageStatus: String,
    val lastMessageOutbound: Boolean,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean
)
