package com.websbaba.nitigrow.presentation.feature.settings

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.PushTokenRepository
import com.websbaba.nitigrow.domain.usecase.auth.LogoutUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val loggingOut: Boolean = false,
    val error: String? = null
)

sealed interface SettingsEffect {
    data object LoggedOut : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logout: LogoutUseCase,
    private val pushRepo: PushTokenRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onLogout() {
        viewModelScope.launch {
            _state.update { it.copy(loggingOut = true, error = null) }
            // Best-effort unregister; never block logout
            runCatching { pushRepo.unregisterCurrentToken() }
            when (val res = logout()) {
                is ApiResult.Success, is ApiResult.Error -> {
                    // Either way, local session cleared by repo; treat as logged out
                    _state.update { it.copy(loggingOut = false) }
                    _effects.send(SettingsEffect.LoggedOut)
                }
            }
        }
    }
}
