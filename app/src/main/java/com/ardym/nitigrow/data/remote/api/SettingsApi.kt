package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.SettingsDto
import com.ardym.nitigrow.data.remote.dto.UpdateAutoRepliesRequest
import com.ardym.nitigrow.data.remote.dto.UpdateBusinessProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface SettingsApi {

    @GET("settings")
    suspend fun get(): SettingsDto

    @PATCH("settings/profile")
    suspend fun updateProfile(@Body body: UpdateBusinessProfileRequest): GenericMessageDto

    @PUT("settings/auto-replies")
    suspend fun updateAutoReplies(@Body body: UpdateAutoRepliesRequest): GenericMessageDto
}
