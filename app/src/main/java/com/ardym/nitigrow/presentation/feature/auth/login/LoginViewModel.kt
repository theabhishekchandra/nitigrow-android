package com.ardym.nitigrow.presentation.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
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
 * Phone-OTP login (design: OTP-mode login). Enter a 10-digit WhatsApp number,
 * request an OTP, then verify on the OTP screen. The design's email-default
 * login variant ships with the auth rework in a later phase.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val requestOtp: RequestOtpUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(LoginUiState.PHONE_LENGTH)
        _state.update { it.copy(phone = digits, error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isLoading) return
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
