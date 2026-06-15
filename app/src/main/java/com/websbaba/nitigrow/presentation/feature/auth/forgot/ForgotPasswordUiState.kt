package com.websbaba.nitigrow.presentation.feature.auth.forgot

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordUiState — 2-step email-link reset flow:
//
//   ENTER_EMAIL  ─►  LINK_SENT
//
// The app does NOT reset the password in-app. It calls POST /auth/forgot-password,
// which emails a single-use reset link (valid 30 min); the user completes the
// reset on the web page that link opens. This mirrors the existing, tested
// backend and avoids an in-app OTP mechanism the backend doesn't implement.
//
// Spec: docs/phase-3-mobile.md §1.2 "Authentication Screens" → Forgot password
// ─────────────────────────────────────────────────────────────────────────────

data class ForgotPasswordUiState(
    val email: String = "",
    val step: ForgotStep = ForgotStep.ENTER_EMAIL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

enum class ForgotStep { ENTER_EMAIL, LINK_SENT }

/**
 * One-shot UI effects (navigation, toasts). Delivered via Channel — never
 * StateFlow — because navigation events must fire exactly once.
 */
sealed interface ForgotEffect {
    data object NavigateBackToLogin : ForgotEffect
    data class Toast(val text: String) : ForgotEffect
}
