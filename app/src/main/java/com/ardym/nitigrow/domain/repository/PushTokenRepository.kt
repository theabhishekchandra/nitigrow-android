package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult

interface PushTokenRepository {
    suspend fun registerCurrentToken(): ApiResult<Unit>
    suspend fun registerToken(token: String): ApiResult<Unit>
    suspend fun unregisterCurrentToken(): ApiResult<Unit>
}
