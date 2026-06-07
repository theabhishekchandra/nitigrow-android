package com.ardym.nitigrow.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Mirrors the backend Mongo Message document:
 * { _id, tenantId, contactId, direction: "inbound"|"outbound",
 *   type: lowercase string, content (string | { text }), status,
 *   createdAt, waMessageId, mediaUrl, ... }
 *
 * The app treats `contactId` AS the conversation id (the backend keys threads
 * by contact, not by a separate conversation document).
 *
 * This DTO binds from TWO producers, so several fields carry `alternate` names
 * and raw fallbacks:
 *   1. The REST endpoints, which return the raw Mongo document above.
 *   2. RealtimeClient.parseNewMessage (which this layer must NOT modify), which
 *      hands Gson a *normalized* object using legacy field names:
 *      { _id, conversationId, text (string), sentAt, outbound (bool), status, type }.
 *
 * `content` is polymorphic on the wire (plain string OR { text }); it is held as
 * a raw [JsonElement] and flattened by [text]. When the realtime path supplies a
 * pre-extracted `text` string instead, [rawText] captures it. This avoids a
 * registered Gson adapter (the shared Gson lives in NetworkModule, off-limits).
 */
data class MessageDto(
    @SerializedName(value = "_id", alternate = ["id"]) val id: String,
    @SerializedName(value = "contactId", alternate = ["conversationId"]) val contactId: String,
    @SerializedName("direction") val direction: String? = null,   // "inbound" | "outbound"
    @SerializedName("type") val type: String = "text",            // lowercase
    @SerializedName("content") val content: JsonElement? = null,
    @SerializedName("text") val rawText: String? = null,          // realtime-normalized path
    @SerializedName("outbound") val rawOutbound: Boolean? = null, // realtime-normalized path
    @SerializedName("status") val status: String = "sent",
    @SerializedName(value = "createdAt", alternate = ["sentAt"]) val createdAt: String? = null,
    @SerializedName("waMessageId") val waMessageId: String? = null,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("mediaId") val mediaId: String? = null
) {
    /** True when the message was sent by the business (outbound). */
    val outbound: Boolean
        get() = rawOutbound ?: direction.equals("outbound", ignoreCase = true)

    /** Flattened text body — from `content` (string|object) or the realtime `text`. */
    val text: String
        get() {
            val c = content
            val fromContent = when {
                c == null || c.isJsonNull -> null
                c.isJsonPrimitive -> c.asString
                c.isJsonObject -> {
                    val obj = c.asJsonObject
                    val field = obj.get("text") ?: obj.get("caption")
                    field?.takeIf { !it.isJsonNull }?.asString
                }
                else -> null
            }
            return fromContent ?: rawText.orEmpty()
        }
}

/**
 * GET messages/conversation/{contactId} response:
 * { messages: [ <Message> ... ] (ascending), contact: { ... } }.
 * The backend paginates with page/limit (no cursor); there is no `nextCursor`.
 */
data class MessagePageResponse(
    @SerializedName("messages") val messages: List<MessageDto> = emptyList()
)

/**
 * POST messages/send body. Backend accepts { contactId, type, text } (and a
 * `content` shorthand). We send `text` for the text path. `clientId` is local
 * only — the backend ignores unknown fields, so it is harmless and lets the
 * caller correlate the optimistic row.
 */
data class SendMessageRequest(
    @SerializedName("contactId") val contactId: String,
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String = "text",
    @SerializedName("clientId") val clientId: String? = null
)

/** POST messages/send response: { message: <Message> }. */
data class SendMessageResponse(
    @SerializedName("message") val message: MessageDto
)
