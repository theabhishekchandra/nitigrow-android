package com.websbaba.nitigrow.core.network

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.data.remote.api.AuthApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Provider

class TokenAuthenticatorTest {

    private val tokenStore: TokenDataStore = mockk()
    private val authApi: AuthApi = mockk()
    private val provider: Provider<AuthApi> = mockk { io.mockk.every { get() } returns authApi }
    private val authenticator = TokenAuthenticator(tokenStore, provider)

    private fun response401(url: String, authorization: String?): Response {
        val request = Request.Builder().url(url).apply {
            authorization?.let { header("Authorization", it) }
        }.build()
        return Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
            .code(401).message("Unauthorized").build()
    }

    @Test
    fun `a 401 on the refresh call is never retried, so a dead refresh token cannot loop`() {
        val result = authenticator.authenticate(
            null, response401("https://api.nitigrow.in/api/auth/token/refresh", "Bearer stale-refresh")
        )

        assertThat(result).isNull()
        // It must not have touched the token store or fired another refresh.
        verify(exactly = 0) { provider.get() }
    }

    @Test
    fun `an unauthenticated request is left alone`() {
        val result = authenticator.authenticate(null, response401("https://api.nitigrow.in/api/campaigns", null))

        assertThat(result).isNull()
        verify(exactly = 0) { provider.get() }
    }

    private fun httpError(code: Int) =
        HttpException(retrofit2.Response.error<Any>(code, "".toResponseBody(null)))

    private fun stubRefreshFailure(error: Throwable) {
        coEvery { tokenStore.refreshTokenBlocking() } returns "refresh-1"
        coEvery { authApi.refreshToken(any(), any()) } throws error
        coEvery { tokenStore.clear() } returns Unit
    }

    @Test
    fun `a dropped connection during refresh keeps the session`() {
        stubRefreshFailure(IOException("network unreachable"))

        val result = authenticator.authenticate(
            null, response401("https://api.nitigrow.in/api/campaigns", "Bearer expired-access")
        )

        assertThat(result).isNull()
        coVerify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `a server error during refresh keeps the session`() {
        stubRefreshFailure(httpError(503))

        authenticator.authenticate(
            null, response401("https://api.nitigrow.in/api/campaigns", "Bearer expired-access")
        )

        coVerify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `a rejected refresh token ends the session`() {
        stubRefreshFailure(httpError(401))

        val result = authenticator.authenticate(
            null, response401("https://api.nitigrow.in/api/campaigns", "Bearer expired-access")
        )

        assertThat(result).isNull()
        coVerify(exactly = 1) { tokenStore.clear() }
    }
}
