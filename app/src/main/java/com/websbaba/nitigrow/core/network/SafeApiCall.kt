package com.websbaba.nitigrow.core.network

import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
        // Prefer the backend's own message (e.g. {"error":"Invalid email or
        // password"}) over the bare HTTP reason ("Unauthorized"), which reads
        // to users like a dead button rather than a rejected request.
        val message = serverMessage(e) ?: e.message().ifBlank { "Something went wrong" }
        ApiResult.Error(code = e.code(), message = message, type = type, cause = e)
    } catch (e: IOException) {
        Timber.w(e, "Network IO")
        ApiResult.Error(message = "No internet connection", type = ErrorType.Network, cause = e)
    } catch (e: JsonParseException) {
        // Payload shape the DTOs don't expect (e.g. object where a list was promised).
        Timber.e(e, "Unreadable response")
        ApiResult.Error(message = UNREADABLE_RESPONSE, type = ErrorType.Parsing, cause = e)
    } catch (e: Exception) {
        Timber.e(e, "Unexpected")
        // Never surface raw exception text (class names, JSON paths) to users.
        ApiResult.Error(message = GENERIC_ERROR, type = ErrorType.Unknown, cause = e)
    }
}

const val UNREADABLE_RESPONSE = "We got an unexpected response from the server. Please try again."
const val GENERIC_ERROR = "Something went wrong. Please try again."

/**
 * Continues a successful call with [transform] — typically mapping the DTO and writing it to
 * Room. Anything it throws (a mapper meeting an unexpected payload, a database error) becomes
 * an [ApiResult.Error] instead of escaping into the caller's coroutine and crashing the app.
 */
suspend inline fun <T, R> ApiResult<T>.andThen(transform: (T) -> ApiResult<R>): ApiResult<R> = when (this) {
    is ApiResult.Success -> try {
        transform(data)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e, "Failed to process response")
        ApiResult.Error(message = UNREADABLE_RESPONSE, type = ErrorType.Parsing, cause = e)
    }
    is ApiResult.Error -> this
}

/**
 * Extracts a human-readable message from an error response body shaped like
 * `{"error":"..."}` or `{"message":"..."}`. Returns null if the body is absent,
 * unreadable, or not JSON — callers fall back to the HTTP reason.
 */
fun serverMessage(e: HttpException): String? = runCatching {
    val body = e.response()?.errorBody()?.string()
    if (body.isNullOrBlank()) return null
    val json = JSONObject(body)
    val msg = json.optString("error").ifBlank { json.optString("message") }
    msg.ifBlank { null }
}.getOrNull()
