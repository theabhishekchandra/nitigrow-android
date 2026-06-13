package com.websbaba.nitigrow.presentation.feature.auth.forgot

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.presentation.base.BaseViewModel
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

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordViewModel — state machine for the 4-step reset flow.
//
//   ENTER_ID            (user types email or phone) ──submit──►  Toast + ENTER_OTP
//   ENTER_OTP           (6 digits)                  ──submit──►  ENTER_NEW_PASSWORD
//   ENTER_NEW_PASSWORD  (new == confirm, len ≥ 6)   ──submit──►  DONE
//   DONE                ──submit──►  Channel<NavigateBackToLogin>
//
// Every transition currently fakes a server call (delay 600 ms on the ID step,
// instant on the rest) and carries the standard dummy-data TODO comment, since
// the real /auth/reset/* endpoints don't exist yet.
//
// Spec: docs/phase-3-mobile.md §1.2 "Forgot password screen"
// ─────────────────────────────────────────────────────────────────────────────

private const val MIN_PASSWORD_LEN = 6
private const val OTP_LEN = 6
private const val FAKE_NETWORK_DELAY_MS = 600L

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    private val _effects = Channel<ForgotEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIdentifierChange(value: String) {
        _state.update { it.copy(identifier = value, error = null) }
    }

    fun onOtpChange(value: String) {
        if (value.length > OTP_LEN || !value.all { it.isDigit() }) return
        _state.update { it.copy(otp = value, error = null) }
    }

    fun onNewPasswordChange(value: String) {
        _state.update { it.copy(newPassword = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value, error = null) }
    }

    /** Advances the flow based on the current step. Validation happens here. */
    fun onPrimaryAction() {
        when (_state.value.step) {
            ForgotStep.ENTER_ID -> submitIdentifier()
            ForgotStep.ENTER_OTP -> submitOtp()
            ForgotStep.ENTER_NEW_PASSWORD -> submitNewPassword()
            ForgotStep.DONE -> emitNavigateBack()
        }
    }

    /** Re-send the OTP — no real network call yet, just a Toast. */
    fun onResendOtp() {
        viewModelScope.launch {
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            _effects.send(ForgotEffect.Toast("OTP resent to ${mask(_state.value.identifier)}"))
        }
    }

    // ── Step 1 — submit identifier ─────────────────────────────────────────
    private fun submitIdentifier() {
        val id = _state.value.identifier.trim()
        if (!isValidIdentifier(id)) {
            _state.update { it.copy(error = "Enter a valid email or phone number.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            delay(FAKE_NETWORK_DELAY_MS)
            val masked = mask(id)
            _state.update {
                it.copy(
                    isSubmitting = false,
                    step = ForgotStep.ENTER_OTP,
                    message = "Code sent to $masked",
                )
            }
            _effects.send(ForgotEffect.Toast("OTP sent to $masked"))
        }
    }

    // ── Step 2 — submit OTP ────────────────────────────────────────────────
    private fun submitOtp() {
        val code = _state.value.otp
        if (code.length != OTP_LEN) {
            _state.update { it.copy(error = "Enter the 6-digit code.") }
            return
        }
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        _state.update {
            it.copy(step = ForgotStep.ENTER_NEW_PASSWORD, error = null, message = null)
        }
    }

    // ── Step 3 — submit new password ───────────────────────────────────────
    private fun submitNewPassword() {
        val s = _state.value
        when {
            s.newPassword.length < MIN_PASSWORD_LEN ->
                _state.update {
                    it.copy(error = "Password must be at least $MIN_PASSWORD_LEN characters.")
                }
            s.newPassword != s.confirmPassword ->
                _state.update { it.copy(error = "Passwords do not match.") }
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
                    delay(FAKE_NETWORK_DELAY_MS)
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            step = ForgotStep.DONE,
                            message = "Password updated",
                        )
                    }
                }
            }
        }
    }

    // ── Step 4 — finish ────────────────────────────────────────────────────
    private fun emitNavigateBack() {
        viewModelScope.launch { _effects.send(ForgotEffect.NavigateBackToLogin) }
    }

    /**
     * Mask an email like `owner@websbaba.in` → `o****@websbaba.in`
     * or a phone like `+91 9810000001`     → `+91 ****0001`.
     * Pure presentation — never trust the masked value as identity.
     */
    private fun mask(raw: String): String {
        val trimmed = raw.trim()
        return if (trimmed.contains('@')) maskEmail(trimmed) else maskPhone(trimmed)
    }

    private fun maskEmail(email: String): String {
        val at = email.indexOf('@').takeIf { it > 0 } ?: return "****"
        val local = email.substring(0, at)
        val domain = email.substring(at)
        val head = local.take(1)
        return "$head****$domain"
    }

    private fun maskPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 4) return "****"
        val last4 = digits.takeLast(4)
        val country = if (phone.trimStart().startsWith("+")) {
            "+" + digits.dropLast(last4.length).take(2)
        } else ""
        return if (country.isNotEmpty()) "$country ****$last4" else "****$last4"
    }

    private fun isValidIdentifier(id: String): Boolean {
        if (id.isBlank()) return false
        if (id.contains('@')) return id.matches(EmailRegex)
        val digits = id.filter { it.isDigit() }
        return digits.length in 10..13
    }

    companion object {
        // Same loose-but-safe email regex used by the rest of the app —
        // pre-compiled so it isn't re-parsed on every keystroke.
        private val EmailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
