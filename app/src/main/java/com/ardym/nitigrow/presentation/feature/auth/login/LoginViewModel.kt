package com.ardym.nitigrow.presentation.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.telemetry.Events
import com.ardym.nitigrow.core.telemetry.Telemetry
import com.ardym.nitigrow.domain.repository.PushTokenRepository
import com.ardym.nitigrow.domain.usecase.auth.LoginWithEmailUseCase
import com.ardym.nitigrow.domain.usecase.auth.RequestOtpUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles both Email/Password login (default) and Phone-OTP login.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmail: LoginWithEmailUseCase,
    private val requestOtp: RequestOtpUseCase,
    private val pushTokenRepo: PushTokenRepository,
    private val telemetry: Telemetry
) : BaseViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onToggleMode(isEmailMode: Boolean) {
        _state.update { it.copy(isEmailMode = isEmailMode, error = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(LoginUiState.PHONE_LENGTH)
        _state.update { it.copy(phone = digits, error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isLoading) return
        
        if (current.isEmailMode) {
            submitEmailLogin(current)
        } else {
            submitPhoneOtpRequest(current)
        }
    }

    private fun submitEmailLogin(current: LoginUiState) {
        if (!current.isEmailValid) {
            _state.update { it.copy(error = "Enter both email and password") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = loginWithEmail(current.email, current.password)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    telemetry.event(Events.LOGIN_EMAIL_VERIFIED)
                    telemetry.setUser(res.data.id, res.data.tenantId)
                    viewModelScope.launch { pushTokenRepo.registerCurrentToken() }
                    _effects.send(LoginEffect.NavigateToHome)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    private fun submitPhoneOtpRequest(current: LoginUiState) {
        if (!current.isPhoneValid) {
            _state.update { it.copy(error = "Enter a valid 10-digit WhatsApp number") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = requestOtp(current.phone)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.send(LoginEffect.NavigateToOtp(current.phone))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
            }
        }
    }
}
