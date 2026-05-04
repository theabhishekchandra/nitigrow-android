package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.AuthResponseDto
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.RequestOtpRequest
import com.ardym.nitigrow.data.remote.dto.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {

    @Headers("No-Auth: true")
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: RequestOtpRequest): GenericMessageDto

    @Headers("No-Auth: true")
    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(): GenericMessageDto
}
