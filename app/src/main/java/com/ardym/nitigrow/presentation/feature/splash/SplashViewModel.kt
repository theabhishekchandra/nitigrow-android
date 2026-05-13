package com.ardym.nitigrow.presentation.feature.splash

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.BuildConfig
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.presentation.base.BaseViewModel
import com.ardym.nitigrow.presentation.navigation.NavRoutes
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
    private val tokenStore: TokenDataStore
) : BaseViewModel() {

    private val _effects = Channel<SplashEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { decideNext() }

    private fun decideNext() {
        viewModelScope.launch {
            delay(MIN_SHOW_MS) // brand reveal
            val onboarded = tokenStore.onboardingDone.first()
            val token = tokenStore.accessTokenBlocking()
            val route = when {
                // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
                // Debug builds skip the auth gate so the design-review screens (Dashboard, Inbox, Contacts,
                // Campaigns, Leads, Billing, Settings) are reachable without a live login backend.
                BuildConfig.DEBUG -> NavRoutes.GRAPH_MAIN
                !onboarded -> NavRoutes.ONBOARDING
                token.isNullOrBlank() -> NavRoutes.GRAPH_AUTH
                else -> NavRoutes.GRAPH_MAIN
            }
            _effects.send(SplashEffect.NavigateTo(route))
        }
    }

    companion object { private const val MIN_SHOW_MS = 600L }
}
