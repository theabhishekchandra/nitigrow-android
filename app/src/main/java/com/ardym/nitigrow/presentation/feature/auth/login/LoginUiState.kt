package com.ardym.nitigrow.presentation.feature.auth.login

data class LoginUiState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isPhoneValid: Boolean
        get() = phone.filter { it.isDigit() }.length in 10..13
}

sealed interface LoginEffect {
    data class NavigateToOtp(val phone: String) : LoginEffect
}
