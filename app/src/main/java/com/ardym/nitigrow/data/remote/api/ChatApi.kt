package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.MessageDto
import com.ardym.nitigrow.data.remote.dto.MessagePageResponse
import com.ardym.nitigrow.data.remote.dto.SendMessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    @GET("conversations/{id}/messages")
    suspend fun page(
        @Path("id") conversationId: String,
        @Query("before") beforeCursor: String? = null,
        @Query("limit") limit: Int = 50
    ): MessagePageResponse

    @POST("messages")
    suspend fun send(@Body body: SendMessageRequest): MessageDto
}
