package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult

interface PushTokenRepository {
    suspend fun registerCurrentToken(): ApiResult<Unit>
    suspend fun registerToken(token: String): ApiResult<Unit>
    suspend fun unregisterCurrentToken(): ApiResult<Unit>
}
