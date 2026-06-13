package com.websbaba.nitigrow.core.network

import com.websbaba.nitigrow.core.storage.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds Authorization: Bearer <token> to every request.
 * Skips auth for endpoints tagged @Headers("No-Auth: true").
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header("No-Auth") != null) {
            return chain.proceed(original.newBuilder().removeHeader("No-Auth").build())
        }
        val token = runBlocking { tokenStore.accessTokenBlocking() }
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else original
        return chain.proceed(request)
    }
}
