package com.websbaba.nitigrow.core.network

import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.data.remote.api.AuthApi
import com.websbaba.nitigrow.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 401 handler for the mobile auth path. On an authenticated request that comes
 * back 401, it calls POST auth/token/refresh with the stored refresh token
 * (Authorization: Bearer <refresh>), persists the rotated access+refresh pair,
 * and retries the original request once with the new access token.
 *
 * On refresh failure (revoked/expired refresh token) it clears the session so
 * the app falls back to the login screen.
 *
 * Uses Provider<AuthApi> to break the Hilt cycle: Retrofit needs the
 * Authenticator, and the Authenticator needs a Retrofit-built AuthApi.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenDataStore,
    private val authApi: Provider<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only retry requests that carried an Authorization header (i.e. were authenticated).
        if (response.request.header("Authorization") == null) return null
        // Avoid infinite loops: give up after a single refresh+retry attempt.
        if (responseCount(response) >= 2) return null

        val refresh = runBlocking { tokenStore.refreshTokenBlocking() }
        if (refresh.isNullOrBlank()) return null

        val tokens = runBlocking {
            runCatching {
                authApi.get().refreshToken(
                    bearerRefresh = "Bearer $refresh",
                    body = RefreshTokenRequest(refresh)
                )
            }.getOrNull()
        }

        val newAccess = tokens?.accessToken
        if (newAccess.isNullOrBlank()) {
            runBlocking { tokenStore.clear() }
            return null
        }

        // Persist the rotated pair (server rotates the refresh token on the mobile path).
        runBlocking {
            val newRefresh = tokens.refreshToken
            if (!newRefresh.isNullOrBlank()) {
                tokenStore.updateTokens(newAccess, newRefresh)
            } else {
                tokenStore.updateAccessToken(newAccess)
            }
        }

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
