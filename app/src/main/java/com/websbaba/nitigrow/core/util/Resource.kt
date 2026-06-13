package com.websbaba.nitigrow.core.util

/**
 * UI-facing state wrapper. ViewModels expose StateFlow<Resource<T>>.
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
    data object Idle : Resource<Nothing>
}
