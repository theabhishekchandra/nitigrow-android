package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.PushApi
import com.websbaba.nitigrow.data.remote.dto.RegisterTokenRequest
import com.websbaba.nitigrow.domain.repository.PushTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRepositoryImpl @Inject constructor(
    private val api: PushApi,
    private val dispatchers: DispatcherProvider
) : PushTokenRepository {

    override suspend fun registerCurrentToken(): ApiResult<Unit> {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }
            .getOrElse {
                Timber.w(it, "FCM token fetch failed")
                return ApiResult.Error(message = "FCM token unavailable")
            }
        return registerToken(token)
    }

    override suspend fun registerToken(token: String): ApiResult<Unit> =
        safeApiCall(dispatchers.io) {
            api.register(RegisterTokenRequest(token = token))
            Unit
        }

    override suspend fun unregisterCurrentToken(): ApiResult<Unit> {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }
            .getOrNull() ?: return ApiResult.Success(Unit)
        return safeApiCall(dispatchers.io) {
            api.unregister(token); Unit
        }
    }
}
