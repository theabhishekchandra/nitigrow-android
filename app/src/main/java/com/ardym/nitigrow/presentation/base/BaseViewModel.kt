package com.ardym.nitigrow.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.util.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Optional base. Provides:
 *  - launchIn(): coroutine launch with a default exception handler so a single
 *    throwing job doesn't crash the whole VM scope.
 *  - asResource(): one-liner ApiResult → Resource conversion for UI.
 */
abstract class BaseViewModel : ViewModel() {

    private val handler = CoroutineExceptionHandler { _, t ->
        Timber.e(t, "Uncaught in ViewModel")
    }

    protected fun launchIn(block: suspend () -> Unit): Job =
        viewModelScope.launch(handler) { block() }

    protected fun <T> ApiResult<T>.toResource(): Resource<T> = when (this) {
        is ApiResult.Success -> Resource.Success(data)
        is ApiResult.Error -> Resource.Error(message, cause)
    }

    protected fun <T> mutableState(initial: T): Pair<MutableStateFlow<T>, StateFlow<T>> {
        val m = MutableStateFlow(initial)
        return m to m.asStateFlow()
    }
}
