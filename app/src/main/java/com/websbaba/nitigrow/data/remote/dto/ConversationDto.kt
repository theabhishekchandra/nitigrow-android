package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * One row from GET messages/conversations (a BARE JSON ARRAY — no envelope).
 *
 * Backend aggregation shape (see inboxController.getConversations):
 *   { contactId: <full Contact document>, lastMessage: <Message|null>, unreadCount }
 *
 * There is no separate conversation id, no pin/mute, and no
 * lastMessage-summary fields on the wire. We derive the flat fields the
 * ConversationMapper consumes from the nested `contact` + `lastMessage`.
 * The contact's `_id` doubles as both the conversation id and the contact id.
 */
data class ConversationDto(
    @SerializedName("contactId") val contact: ContactSummaryDto,
    @SerializedName("lastMessage") val lastMessageObj: MessageDto? = null,
    @SerializedName("unreadCount") val unreadCount: Int = 0
) {
    /** Contact id is used as the conversation id throughout the app. */
    val id: String get() = contact.id
    val contactId: String get() = contact.id
    val contactName: String get() = contact.name ?: contact.phone
    val contactPhone: String get() = contact.phone
    val avatarUrl: String? get() = null

    val lastMessage: String get() = lastMessageObj?.text.orEmpty()
    /**
     * ISO-8601 timestamp of the last message. A chat with no messages has none, so it
     * reads as epoch-zero (rendered blank, sorted last) rather than borrowing the
     * contact's `updatedAt`, which would invent a message time that never happened.
     */
    val lastMessageAt: String
        get() = lastMessageObj?.createdAt ?: NO_MESSAGE_AT
    val lastMessageStatus: String get() = lastMessageObj?.status ?: "sent"
    val lastMessageOutbound: Boolean get() = lastMessageObj?.outbound ?: false

    val isPinned: Boolean get() = contact.isPinned
    val isMuted: Boolean get() = contact.isMuted

    /** When the 24h reply window closes, as the server computes it; null = no open window. */
    val windowExpiresAt: String? get() = contact.windowExpiresAt

    companion object {
        const val NO_MESSAGE_AT = "1970-01-01T00:00:00Z"
    }
}

/** The subset of the embedded Contact document the inbox list needs. */
data class ContactSummaryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String = "",
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("windowExpiresAt") val windowExpiresAt: String? = null,
    @SerializedName("isPinned") val isPinned: Boolean = false,
    @SerializedName("isMuted") val isMuted: Boolean = false
)
