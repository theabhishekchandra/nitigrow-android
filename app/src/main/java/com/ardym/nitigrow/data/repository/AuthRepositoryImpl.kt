package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.remote.api.AuthApi
import com.ardym.nitigrow.data.remote.dto.LoginRequest
import com.ardym.nitigrow.data.remote.dto.RegisterRequest
import com.ardym.nitigrow.data.remote.dto.RequestOtpRequest
import com.ardym.nitigrow.data.remote.dto.UserDto
import com.ardym.nitigrow.data.remote.dto.VerifyOtpRequest
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.model.UserRole
import com.ardym.nitigrow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenDataStore,
    private val dispatchers: DispatcherProvider
) : AuthRepository {

    /**
     * Email/password login. On success the backend (mobile path) returns both
     * tokens in the body; persist them encrypted and return the domain user.
     */
    suspend fun login(email: String, password: String): ApiResult<User> =
        when (val res = safeApiCall(dispatchers.io) { api.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Success -> {
                val dto = res.data
                val refresh = dto.refreshToken
                    ?: return ApiResult.Error(message = "Login failed: missing refresh token")
                tokenStore.saveSession(
                    access = dto.accessToken,
                    refresh = refresh,
                    userId = dto.user.id,
                    tenantId = dto.user.tenantId
                )
                ApiResult.Success(dto.user.toUser())
            }
            is ApiResult.Error -> res
        }

    /**
     * Register a new business owner, then immediately log in to obtain a refresh
     * token (register only returns the access token + user on the body for mobile).
     */
    suspend fun register(
        businessName: String,
        email: String,
        password: String,
        phone: String
    ): ApiResult<User> {
        val res = safeApiCall(dispatchers.io) {
            api.register(RegisterRequest(businessName.trim(), email.trim(), password, phone.trim()))
        }
        return when (res) {
            is ApiResult.Success -> login(email, password)
            is ApiResult.Error -> res
        }
    }

    /** Validates the current session and refreshes the user/tenant snapshot. */
    suspend fun getMe(): ApiResult<User> =
        when (val res = safeApiCall(dispatchers.io) { api.me() }) {
            is ApiResult.Success -> ApiResult.Success(res.data.user.toUser())
            is ApiResult.Error -> res
        }

    // --- Legacy phone-OTP path (kept for the existing OTP feature) ---

    override suspend fun requestOtp(phone: String): ApiResult<Unit> =
        safeApiCall(dispatchers.io) {
            api.requestOtp(RequestOtpRequest(phone))
            Unit
        }

    override suspend fun verifyOtp(phone: String, code: String): ApiResult<User> =
        when (val res = safeApiCall(dispatchers.io) { api.verifyOtp(VerifyOtpRequest(phone, code)) }) {
            is ApiResult.Success -> {
                val dto = res.data
                tokenStore.saveSession(
                    access = dto.accessToken,
                    refresh = dto.refreshToken.orEmpty(),
                    userId = dto.user.id,
                    tenantId = dto.user.tenantId
                )
                ApiResult.Success(dto.user.toUser())
            }
            is ApiResult.Error -> res
        }

    override suspend fun logout(): ApiResult<Unit> {
        val res = safeApiCall(dispatchers.io) { api.logout(); Unit }
        tokenStore.clear()
        return res
    }

    override fun observeAuthState(): Flow<Boolean> =
        tokenStore.accessToken.map { !it.isNullOrBlank() }

    /**
     * Local mapping that tolerates the missing `phone` field returned by the
     * email/password endpoints (login/me), without touching the shared mapper.
     */
    private fun UserDto.toUser(): User = User(
        id = id,
        tenantId = tenantId,
        name = name,
        email = email,
        phone = phone, // login/me omit phone; DTO defaults it to "".
        role = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.AGENT)
    )
}
