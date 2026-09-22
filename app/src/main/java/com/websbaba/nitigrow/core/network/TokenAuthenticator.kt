package com.websbaba.nitigrow.core.network

import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.data.remote.api.AuthApi
import com.websbaba.nitigrow.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 401 handler for the mobile auth path. On an authenticated request that comes
 * back 401, it calls POST auth/token/refresh with the stored refresh token
 * (Authorization: Bearer <refresh>), persists the rotated access+refresh pair,
 * and retries the original request once with the new access token.
 *
 * When the server rejects the refresh token (revoked/expired) it clears the session so
 * the app falls back to the login screen; transient failures leave the session intact.
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
        // A 401 on the refresh call itself means the refresh token is dead. Retrying would
        // send another refresh through this same client, whose 401 would trigger another
        // — an endless chain that leaves the app stuck on its splash screen instead of
        // reaching login. Give up so the caller clears the session.
        if (isRefreshCall(response.request)) return null
        // Avoid infinite loops: give up after a single refresh+retry attempt.
        if (responseCount(response) >= 2) return null

        val refresh = runBlocking { tokenStore.refreshTokenBlocking() }
        if (refresh.isNullOrBlank()) return null

        val refreshed = runBlocking {
            runCatching {
                authApi.get().refreshToken(
                    bearerRefresh = "Bearer $refresh",
                    body = RefreshTokenRequest(refresh)
                )
            }
        }

        val failure = refreshed.exceptionOrNull()
        if (failure != null) {
            // Only a server verdict that the refresh token is dead ends the session. A
            // timeout, dropped connection or 5xx (e.g. the network flapping) says nothing
            // about the token, so keep it and let the next request try again.
            if (failure is HttpException && failure.code() in REFRESH_REJECTED_CODES) {
                runBlocking { tokenStore.clear() }
            }
            return null
        }

        val tokens = refreshed.getOrThrow()
        val newAccess = tokens.accessToken
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

    private fun isRefreshCall(request: Request): Boolean =
        request.url.encodedPath.endsWith(REFRESH_PATH)

    private fun responseCount(response: Response): Int {
        var r: Response? = response.priorResponse
        var count = 1
        while (r != null) { count++; r = r.priorResponse }
        return count
    }

    private companion object {
        const val REFRESH_PATH = "auth/token/refresh"
        val REFRESH_REJECTED_CODES = setOf(400, 401, 403)
    }
}
