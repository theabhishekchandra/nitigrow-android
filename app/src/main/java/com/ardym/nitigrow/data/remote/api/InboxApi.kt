package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.ConversationListResponse
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.TogglePinRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InboxApi {

    @GET("conversations")
    suspend fun list(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 50
    ): ConversationListResponse

    @POST("conversations/{id}/read")
    suspend fun markRead(@Path("id") id: String): GenericMessageDto

    @PATCH("conversations/{id}/pin")
    suspend fun togglePin(
        @Path("id") id: String,
        @Body body: TogglePinRequest
    ): GenericMessageDto
}
