package com.ardym.nitigrow.presentation.feature.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.core.telemetry.Events
import com.ardym.nitigrow.core.telemetry.Telemetry
import com.ardym.nitigrow.domain.repository.PushTokenRepository
import com.ardym.nitigrow.domain.usecase.auth.RequestOtpUseCase
import com.ardym.nitigrow.domain.usecase.auth.VerifyOtpUseCase
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
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val verifyOtp: VerifyOtpUseCase,
    private val requestOtp: RequestOtpUseCase,
    private val tokenStore: TokenDataStore,
    private val pushTokenRepo: PushTokenRepository,
    private val telemetry: Telemetry
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        OtpUiState(phone = savedState.get<String>("phone").orEmpty())
    )
    val state: StateFlow<OtpUiState> = _state.asStateFlow()

    private val _effects = Channel<OtpEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { startResendTimer() }

    fun onCodeChange(value: String) {
        if (value.length > 6 || !value.all { it.isDigit() }) return
        _state.update { it.copy(code = value, error = null) }
    }

    fun onSubmit() {
        val s = _state.value
        if (!s.canSubmit) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = verifyOtp(s.phone, s.code)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    telemetry.event(Events.LOGIN_OTP_VERIFIED)
                    telemetry.setUser(res.data.id, res.data.tenantId)
                    viewModelScope.launch { pushTokenRepo.registerCurrentToken() }
                    _effects.send(OtpEffect.NavigateToHome)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    fun onResend() {
        val phone = _state.value.phone
        if (!_state.value.canResend) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val res = requestOtp(phone)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.send(OtpEffect.ShowMessage("OTP resent"))
                    startResendTimer()
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    fun enableBiometric(enabled: Boolean) {
        viewModelScope.launch { tokenStore.setBiometricEnabled(enabled) }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            for (i in RESEND_COOLDOWN downTo 1) {
                _state.update { it.copy(resendSeconds = i) }
                delay(1_000)
            }
            _state.update { it.copy(resendSeconds = 0) }
        }
    }

    companion object { private const val RESEND_COOLDOWN = 30 }
}
