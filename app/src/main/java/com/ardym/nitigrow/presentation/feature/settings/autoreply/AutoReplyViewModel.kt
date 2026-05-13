package com.ardym.nitigrow.presentation.feature.settings.autoreply

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

private const val SIMULATED_SAVE_LATENCY_MS = 600L

@HiltViewModel
class AutoReplyViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(DummyAutoReplyData.config())
    val state: StateFlow<AutoReplyUiState> = _state.asStateFlow()

    private val _effects = Channel<AutoReplyEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onWelcomeEnabled(v: Boolean) = _state.update { it.copy(welcomeEnabled = v) }
    fun onWelcomeMessage(v: String) = _state.update { it.copy(welcomeMessage = v) }
    fun onAwayEnabled(v: Boolean) = _state.update { it.copy(awayEnabled = v) }
    fun onAwayMessage(v: String) = _state.update { it.copy(awayMessage = v) }
    fun onAwayStart(v: LocalTime) = _state.update { it.copy(awayStart = v) }
    fun onAwayEnd(v: LocalTime) = _state.update { it.copy(awayEnd = v) }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            // TODO: Replace simulated latency with real `tenantRepository.updateAutoReply(...)`.
            delay(SIMULATED_SAVE_LATENCY_MS)
            _state.update { it.copy(isSaving = false) }
            _effects.send(AutoReplyEffect.Toast("Auto-reply saved"))
        }
    }
}
