package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Domain interface. Implementation in data/repository/AuthRepositoryImpl.
 * UseCases depend on this — never on Retrofit/Room directly.
 */
interface AuthRepository {
    suspend fun requestOtp(phone: String): ApiResult<Unit>
    suspend fun verifyOtp(phone: String, code: String): ApiResult<User>
    suspend fun logout(): ApiResult<Unit>
    fun observeAuthState(): Flow<Boolean>
}
