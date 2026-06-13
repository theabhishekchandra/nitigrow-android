package com.websbaba.nitigrow.presentation.feature.settings.autoreply

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.SettingsRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AutoReplyViewModel @Inject constructor(
    private val settings: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(AutoReplyUiState())
    val state: StateFlow<AutoReplyUiState> = _state.asStateFlow()

    private val _effects = Channel<AutoReplyEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            when (val r = settings.get()) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        welcomeEnabled = r.data.welcomeEnabled,
                        welcomeMessage = r.data.welcomeMessage,
                        awayEnabled = r.data.awayEnabled,
                        awayMessage = r.data.awayMessage
                    )
                }
                is ApiResult.Error -> _effects.send(AutoReplyEffect.Toast(r.message))
            }
        }
    }

    fun onWelcomeEnabled(v: Boolean) = _state.update { it.copy(welcomeEnabled = v) }
    fun onWelcomeMessage(v: String) = _state.update { it.copy(welcomeMessage = v) }
    fun onAwayEnabled(v: Boolean) = _state.update { it.copy(awayEnabled = v) }
    fun onAwayMessage(v: String) = _state.update { it.copy(awayMessage = v) }
    // Away time window is UI-only for now (backend stores enabled+message, not hours).
    fun onAwayStart(v: LocalTime) = _state.update { it.copy(awayStart = v) }
    fun onAwayEnd(v: LocalTime) = _state.update { it.copy(awayEnd = v) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val res = settings.updateAutoReplies(
                welcomeEnabled = s.welcomeEnabled,
                welcomeMessage = s.welcomeMessage,
                awayEnabled = s.awayEnabled,
                awayMessage = s.awayMessage
            )
            _state.update { it.copy(isSaving = false) }
            when (res) {
                is ApiResult.Success -> _effects.send(AutoReplyEffect.Toast("Auto-reply saved"))
                is ApiResult.Error -> _effects.send(AutoReplyEffect.Toast(res.message))
            }
        }
    }
}
