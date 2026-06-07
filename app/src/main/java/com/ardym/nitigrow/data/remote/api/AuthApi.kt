package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.AuthResponseDto
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.LoginRequest
import com.ardym.nitigrow.data.remote.dto.MeResponseDto
import com.ardym.nitigrow.data.remote.dto.RefreshResponseDto
import com.ardym.nitigrow.data.remote.dto.RefreshTokenRequest
import com.ardym.nitigrow.data.remote.dto.RegisterRequest
import com.ardym.nitigrow.data.remote.dto.RequestOtpRequest
import com.ardym.nitigrow.data.remote.dto.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Email/password auth against api.nitigrow.in (BASE_URL ends in /api/).
 * Every request also carries `x-client: mobile` via the network interceptor.
 *
 * - login/register need no bearer (tagged No-Auth so the interceptor skips it).
 * - me is authenticated (bearer added by the interceptor).
 * - token/refresh sends the refresh token as a Bearer header explicitly and is
 *   tagged No-Auth so the interceptor does not overwrite it with the access token.
 */
interface AuthApi {

    @Headers("No-Auth: true")
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponseDto

    @Headers("No-Auth: true")
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponseDto

    @GET("auth/me")
    suspend fun me(): MeResponseDto

    /**
     * Mobile refresh: no cookie, no CSRF. The refresh token goes in the
     * Authorization header (Bearer) and also in the body as a fallback.
     */
    @Headers("No-Auth: true")
    @POST("auth/token/refresh")
    suspend fun refreshToken(
        @Header("Authorization") bearerRefresh: String,
        @Body body: RefreshTokenRequest
    ): RefreshResponseDto

    @POST("auth/logout")
    suspend fun logout(): GenericMessageDto

    // --- Legacy phone-OTP endpoints (kept for the existing OTP feature) ---

    @Headers("No-Auth: true")
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: RequestOtpRequest): GenericMessageDto

    @Headers("No-Auth: true")
    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): AuthResponseDto
}
