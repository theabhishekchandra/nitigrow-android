package com.ardym.nitigrow.core.network

/**
 * Repository-layer result. Domain code consumes this and maps to Resource for UI.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(
        val code: Int? = null,
        val message: String,
        val type: ErrorType = ErrorType.Unknown,
        val cause: Throwable? = null
    ) : ApiResult<Nothing>
}

enum class ErrorType {
    Network,        // no internet, timeout
    Unauthorized,   // 401
    Forbidden,      // 403
    NotFound,       // 404
    Server,         // 5xx
    Parsing,        // JSON malformed
    Unknown
}
