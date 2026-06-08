package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.RegisterTokenRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

// FCM device-token registration. Targets the backend device registry at
// POST/DELETE `notifications/devices` (added in commit d9886dd). Calls fail
// gracefully via safeApiCall if the network is unavailable.
interface PushApi {

    @POST("notifications/devices")
    suspend fun register(@Body body: RegisterTokenRequest): GenericMessageDto

    @HTTP(method = "DELETE", path = "notifications/devices", hasBody = false)
    suspend fun unregister(@Query("token") token: String): GenericMessageDto
}
