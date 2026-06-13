package com.websbaba.nitigrow.presentation.feature.splash

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.data.repository.AuthRepositoryImpl
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import com.websbaba.nitigrow.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEffect {
    data class NavigateTo(val route: String) : SplashEffect
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenDataStore,
    private val authRepository: AuthRepositoryImpl
) : BaseViewModel() {

    private val _effects = Channel<SplashEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { decideNext() }

    private fun decideNext() {
        viewModelScope.launch {
            delay(MIN_SHOW_MS) // brand reveal
            val onboarded = tokenStore.onboardingDone.first()
            val route = when {
                !onboarded -> NavRoutes.ONBOARDING
                isSessionValid() -> NavRoutes.GRAPH_MAIN
                else -> NavRoutes.GRAPH_AUTH
            }
            _effects.send(SplashEffect.NavigateTo(route))
        }
    }

    /**
     * A non-blank token is not enough — it may be expired or revoked. Validate
     * against GET auth/me. A 401 triggers a transparent refresh via the OkHttp
     * authenticator; if the refresh also fails the session is cleared and this
     * returns false, routing the user to the auth graph.
     */
    private suspend fun isSessionValid(): Boolean {
        val token = tokenStore.accessTokenBlocking()
        if (token.isNullOrBlank()) return false
        return authRepository.getMe() is ApiResult.Success
    }

    companion object { private const val MIN_SHOW_MS = 600L }
}
