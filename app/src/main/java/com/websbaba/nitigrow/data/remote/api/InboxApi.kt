package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.ConversationDto
import com.websbaba.nitigrow.data.remote.dto.GenericMessageDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * Inbox conversation list. BASE_URL already ends in /api/.
 *
 * GET messages/conversations returns a BARE JSON ARRAY of conversations
 * (no { data } envelope). Conversations are keyed by contactId.
 *
 * Pin/mute have no backend route and were dropped.
 */
interface InboxApi {

    @GET("messages/conversations")
    suspend fun list(): List<ConversationDto>

    /** PATCH messages/{contactId}/read — clears the inbound unread count. */
    @PATCH("messages/{contactId}/read")
    suspend fun markRead(@Path("contactId") contactId: String): GenericMessageDto
}
