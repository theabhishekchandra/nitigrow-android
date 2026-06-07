package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Email/password auth DTOs. The mobile path always sends `x-client: mobile`
 * (added by the network interceptor), which makes the backend include the
 * refreshToken in the JSON body instead of the httpOnly cookie used by web.
 */

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("businessName") val businessName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String
)

/**
 * Body for POST auth/token/refresh. The refresh token is also sent as a
 * Bearer header; the body is a belt-and-braces fallback the backend accepts.
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * Login response. `refreshToken` is only present on the mobile path. Register
 * returns access + user but no refreshToken (web sets it as a cookie), so the
 * repository follows register with a login to obtain both tokens.
 */
data class AuthResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: UserDto
)

/** Response of POST auth/token/refresh (mobile): both rotated tokens. */
data class RefreshResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null
)

/** GET auth/me hydration payload. */
data class MeResponseDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("tenant") val tenant: TenantDto? = null
)

/**
 * User payload. `phone` is non-null to keep the shared User/Profile mappers
 * compiling; the email/password endpoints (login/me) omit it, so Gson leaves it
 * empty/null at runtime — callers that need a guaranteed value coalesce it.
 */
data class UserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String = "",
    @SerializedName("role") val role: String
)

data class GenericMessageDto(
    @SerializedName("message") val message: String?
)

/**
 * Legacy phone-OTP request bodies. Retained so the existing OTP feature and the
 * domain AuthRepository interface keep compiling while the primary auth path is
 * email/password. Safe to remove once the OTP vertical is retired.
 */
data class RequestOtpRequest(
    @SerializedName("phone") val phone: String
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String,
    @SerializedName("deviceId") val deviceId: String? = null
)
