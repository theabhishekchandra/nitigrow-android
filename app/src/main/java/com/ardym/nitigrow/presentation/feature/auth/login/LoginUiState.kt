package com.ardym.nitigrow.presentation.feature.auth.login

data class LoginUiState(
    /** 10-digit national number; the +91 country code is fixed in the UI. */
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isPhoneValid: Boolean
        get() = phone.length == PHONE_LENGTH && phone.all { it.isDigit() }

    val canSubmit: Boolean
        get() = isPhoneValid && !isLoading

    companion object {
        const val PHONE_LENGTH = 10
    }
}

sealed interface LoginEffect {
    /** OTP requested successfully; navigate to the verify screen for [phone]. */
    data class NavigateToOtp(val phone: String) : LoginEffect
}
