package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.remote.api.AuthApi
import com.ardym.nitigrow.data.remote.dto.RequestOtpRequest
import com.ardym.nitigrow.data.remote.dto.VerifyOtpRequest
import com.ardym.nitigrow.domain.model.User
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
                    refresh = dto.refreshToken,
                    userId = dto.user.id,
                    tenantId = dto.user.tenantId
                )
                ApiResult.Success(dto.user.toDomain())
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
}
