package com.ardym.nitigrow.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Wraps a Retrofit suspend call. Maps exceptions → ApiResult.Error with classified type.
 * Repositories call this so UseCase/UI never see raw IOException/HttpException.
 */
suspend inline fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> T
): ApiResult<T> = withContext(dispatcher) {
    try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        Timber.w(e, "HTTP ${e.code()}")
        val type = when (e.code()) {
            401 -> ErrorType.Unauthorized
            403 -> ErrorType.Forbidden
            404 -> ErrorType.NotFound
            in 500..599 -> ErrorType.Server
            else -> ErrorType.Unknown
        }
        ApiResult.Error(code = e.code(), message = e.message(), type = type, cause = e)
    } catch (e: IOException) {
        Timber.w(e, "Network IO")
        ApiResult.Error(message = "No internet connection", type = ErrorType.Network, cause = e)
    } catch (e: Exception) {
        Timber.e(e, "Unexpected")
        ApiResult.Error(message = e.message ?: "Unknown error", type = ErrorType.Unknown, cause = e)
    }
}
