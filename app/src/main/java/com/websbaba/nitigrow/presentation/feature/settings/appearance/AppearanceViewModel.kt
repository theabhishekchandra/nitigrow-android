package com.websbaba.nitigrow.presentation.feature.settings.appearance

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.storage.ThemeDataStore
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import com.websbaba.nitigrow.ui.theme.AppTheme
import com.websbaba.nitigrow.ui.theme.PlanTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Appearance settings sub-screen: the persisted theme choice
 * plus the workspace's plan tier (which decides what is unlocked).
 */
data class AppearanceUiState(
    val selected: AppTheme = AppTheme.Default,
    val tier: PlanTier = PlanTier.STARTER
)

sealed interface AppearanceEffect {
    /** Tapped theme requires a higher plan — show the upgrade prompt. */
    data class UpgradeRequired(val theme: AppTheme) : AppearanceEffect
}

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themeStore: ThemeDataStore,
    private val billing: BillingRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(AppearanceUiState())
    val state: StateFlow<AppearanceUiState> = _state.asStateFlow()

    private val _effects = Channel<AppearanceEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        themeStore.theme
            .onEach { theme -> _state.update { it.copy(selected = theme) } }
            .launchIn(viewModelScope)
        billing.observeSubscription()
            .onEach { sub -> _state.update { it.copy(tier = PlanTier.fromPlanId(sub?.planId)) } }
            .launchIn(viewModelScope)
        // Best-effort refresh so a just-upgraded plan unlocks without app restart;
        // the cached subscription above keeps the screen usable offline.
        viewModelScope.launch { billing.refreshSubscription() }
    }

    fun onSelect(theme: AppTheme) {
        if (theme.isUnlockedFor(_state.value.tier)) {
            viewModelScope.launch { themeStore.setTheme(theme) }
        } else {
            viewModelScope.launch { _effects.send(AppearanceEffect.UpgradeRequired(theme)) }
        }
    }
}
