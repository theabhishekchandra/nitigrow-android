package com.websbaba.nitigrow.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.storage.ThemeDataStore
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.ui.theme.AppTheme
import com.websbaba.nitigrow.ui.theme.PlanTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Resolves the effective [AppTheme] for the whole app: the persisted selection,
 * downgraded to [AppTheme.Default] whenever the tenant's plan tier no longer
 * unlocks it (e.g. after a subscription lapses). Scoped to MainActivity so a
 * single instance drives NitiGrowTheme across the entire composition.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    themeDataStore: ThemeDataStore,
    billingRepository: BillingRepository
) : ViewModel() {

    val planTier: StateFlow<PlanTier> = billingRepository.observeSubscription()
        .map { subscription -> PlanTier.fromPlanId(subscription?.planId) }
        .catch { emit(PlanTier.STARTER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanTier.STARTER)

    val appTheme: StateFlow<AppTheme> = combine(themeDataStore.theme, planTier) { selected, tier ->
        if (selected.isUnlockedFor(tier)) selected else AppTheme.Default
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.Default)
}
