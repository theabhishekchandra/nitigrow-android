package com.ardym.nitigrow.presentation.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.data.repository.AuthRepositoryImpl
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
 * Email/password login. Depends on the concrete [AuthRepositoryImpl] because the
 * email/password methods are not yet declared on the domain AuthRepository
 * interface (interface is owned by another vertical — see follow-ups).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) : BaseViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.isEmailValid) {
            _state.update { it.copy(error = "Enter a valid email address") }
            return
        }
        if (!current.isPasswordValid) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = authRepository.login(current.email, current.password)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.send(LoginEffect.NavigateToHome)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
            }
        }
    }
}
