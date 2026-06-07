package com.ardym.nitigrow.presentation.feature.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmailValid: Boolean
        get() = EMAIL_REGEX.matches(email.trim())

    val isPasswordValid: Boolean
        get() = password.length >= MIN_PASSWORD_LENGTH

    val canSubmit: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

sealed interface LoginEffect {
    /** Login succeeded and tokens are persisted; navigate into the app. */
    data object NavigateToHome : LoginEffect
}
