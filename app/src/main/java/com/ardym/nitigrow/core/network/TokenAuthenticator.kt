package com.ardym.nitigrow.core.network

import com.ardym.nitigrow.core.storage.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 401 handler. Calls /auth/refresh, retries original request once.
 * Uses Provider<RefreshTokenApi> to break Hilt cycle (Retrofit needs Authenticator,
 * Authenticator needs Retrofit-built api).
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenDataStore,
    private val refreshApi: Provider<RefreshTokenApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null

        val refresh = runBlocking { tokenStore.refreshToken.first() } ?: return null

        val newAccess = runBlocking {
            runCatching { refreshApi.get().refresh(RefreshRequest(refresh)).accessToken }
                .getOrNull()
        } ?: run {
            runBlocking { tokenStore.clear() }
            return null
        }

        runBlocking { tokenStore.updateAccessToken(newAccess) }
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response.priorResponse
        var count = 1
        while (r != null) { count++; r = r.priorResponse }
        return count
    }
}

interface RefreshTokenApi {
    @retrofit2.http.Headers("No-Auth: true")
    @retrofit2.http.POST("auth/refresh")
    suspend fun refresh(@retrofit2.http.Body body: RefreshRequest): RefreshResponse
}

data class RefreshRequest(val refreshToken: String)
data class RefreshResponse(val accessToken: String)
