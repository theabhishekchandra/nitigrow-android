package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.MessagePageResponse
import com.ardym.nitigrow.data.remote.dto.SendMessageRequest
import com.ardym.nitigrow.data.remote.dto.SendMessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Chat thread API. BASE_URL already ends in /api/. Threads are keyed by
 * contactId (the app treats contactId AS the conversation id).
 *
 * The backend paginates with page/limit (1-based page, no cursor) and returns
 * { messages: [...] } sorted ascending. Pin/mute have no backend route.
 */
interface ChatApi {

    @GET("messages/conversation/{contactId}")
    suspend fun page(
        @Path("contactId") contactId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): MessagePageResponse

    @POST("messages/send")
    suspend fun send(@Body body: SendMessageRequest): SendMessageResponse
}
