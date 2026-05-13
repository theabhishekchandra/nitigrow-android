package com.ardym.nitigrow.presentation.feature.settings.waba

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SIMULATED_REVERIFY_LATENCY_MS = 600L

sealed interface WabaNumberEffect {
    data class Toast(val text: String) : WabaNumberEffect
}

@HiltViewModel
class WabaNumberViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(DummyWabaData.status())
    val state: StateFlow<WabaNumberUiState> = _state.asStateFlow()

    private val _effects = Channel<WabaNumberEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        // TODO: Call `tenantRepository.refreshWabaStatus()` once available;
        //  for now this is a no-op stub so the UI can mount.
    }

    fun reverify() {
        viewModelScope.launch {
            // TODO: Trigger Meta re-verification flow via backend.
            delay(SIMULATED_REVERIFY_LATENCY_MS)
            _effects.send(WabaNumberEffect.Toast("Re-verification request sent"))
        }
    }
}
