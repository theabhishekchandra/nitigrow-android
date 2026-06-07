package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.RegisterTokenRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

// FCM device-token registration.
//
// NOTE: the backend does not yet expose a device-registration route (the
// notifications router is in-app CRUD only). These calls target
// `notifications/devices`; until the backend adds it, registration fails
// gracefully via safeApiCall. Wire the real route in Phase 3.
interface PushApi {

    @POST("notifications/devices")
    suspend fun register(@Body body: RegisterTokenRequest): GenericMessageDto

    @HTTP(method = "DELETE", path = "notifications/devices", hasBody = false)
    suspend fun unregister(@Query("token") token: String): GenericMessageDto
}
