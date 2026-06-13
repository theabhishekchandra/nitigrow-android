package com.websbaba.nitigrow.presentation.feature.auth.otp

data class OtpUiState(
    val phone: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val resendSeconds: Int = 0,
    val error: String? = null
) {
    val canSubmit: Boolean get() = code.length == 6 && !isLoading
    val canResend: Boolean get() = resendSeconds == 0 && !isLoading
}

sealed interface OtpEffect {
    data object NavigateToHome : OtpEffect
    data class ShowMessage(val message: String) : OtpEffect
}
