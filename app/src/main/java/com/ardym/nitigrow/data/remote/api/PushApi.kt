package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.RegisterTokenRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface PushApi {

    @POST("devices/register")
    suspend fun register(@Body body: RegisterTokenRequest): GenericMessageDto

    @HTTP(method = "DELETE", path = "devices/register", hasBody = false)
    suspend fun unregister(@Query("token") token: String): GenericMessageDto
}
