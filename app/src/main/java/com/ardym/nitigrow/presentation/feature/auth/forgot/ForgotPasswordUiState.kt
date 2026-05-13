package com.ardym.nitigrow.presentation.feature.auth.forgot

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordUiState — 4-step reset flow:
//
//   ENTER_ID  ─►  ENTER_OTP  ─►  ENTER_NEW_PASSWORD  ─►  DONE
//
// All fields live in one state object so back/forward navigation through the
// flow can preserve previously typed values. `error` is per-step (the screen
// clears it on input change); `message` is informational (e.g. masked
// destination hint after OTP is sent) and survives until the next step.
//
// Spec: docs/phase-3-mobile.md §1.2 "Authentication Screens" → Forgot password
// ─────────────────────────────────────────────────────────────────────────────

data class ForgotPasswordUiState(
    val identifier: String = "",
    val step: ForgotStep = ForgotStep.ENTER_ID,
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

enum class ForgotStep { ENTER_ID, ENTER_OTP, ENTER_NEW_PASSWORD, DONE }

/**
 * One-shot UI effects (navigation, toasts). Delivered via Channel — never
 * StateFlow — because navigation events must fire exactly once.
 */
sealed interface ForgotEffect {
    data object NavigateBackToLogin : ForgotEffect
    data class Toast(val text: String) : ForgotEffect
}
