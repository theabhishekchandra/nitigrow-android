package com.websbaba.nitigrow.presentation.feature.auth.forgot

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.AuthRepository
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

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordViewModel — 2-step email-link reset flow.
//
//   ENTER_EMAIL   (user types email)  ──submit──►  POST /auth/forgot-password
//                                                   └─ on success ─► LINK_SENT
//   LINK_SENT     (confirmation)       ──"Back to login"──► NavigateBackToLogin
//                 (also offers "Resend email")
//
// The actual password change happens on the web page the emailed link opens —
// the app never sees the reset token. The backend always returns 200 with a
// generic message (anti-enumeration), so a successful call only means the
// request was accepted, never that the email is registered.
//
// Spec: docs/phase-3-mobile.md §1.2 "Forgot password screen"
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    private val _effects = Channel<ForgotEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    /** Advances the flow based on the current step. */
    fun onPrimaryAction() {
        when (_state.value.step) {
            ForgotStep.ENTER_EMAIL -> submitEmail()
            ForgotStep.LINK_SENT -> emitNavigateBack()
        }
    }

    /** Re-send the reset email to the same address from the confirmation step. */
    fun onResend() = sendResetLink(resend = true)

    // ── Step 1 — request the reset email ───────────────────────────────────────
    private fun submitEmail() {
        if (!isValidEmail(_state.value.email.trim())) {
            _state.update { it.copy(error = "Enter a valid email address.") }
            return
        }
        sendResetLink(resend = false)
    }

    private fun sendResetLink(resend: Boolean) {
        val email = _state.value.email.trim()
        if (!isValidEmail(email)) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            when (val res = authRepository.forgotPassword(email)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isSubmitting = false, step = ForgotStep.LINK_SENT) }
                    if (resend) _effects.send(ForgotEffect.Toast("Reset link sent again"))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSubmitting = false, error = res.message)
                }
            }
        }
    }

    // ── Step 2 — finish ────────────────────────────────────────────────────────
    private fun emitNavigateBack() {
        viewModelScope.launch { _effects.send(ForgotEffect.NavigateBackToLogin) }
    }

    private fun isValidEmail(value: String): Boolean = value.matches(EmailRegex)

    companion object {
        // Same loose-but-safe email regex used by the rest of the app —
        // pre-compiled so it isn't re-parsed on every keystroke.
        private val EmailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
