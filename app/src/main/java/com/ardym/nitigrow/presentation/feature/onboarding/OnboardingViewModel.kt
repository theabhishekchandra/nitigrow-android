package com.ardym.nitigrow.presentation.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tokenStore: TokenDataStore
) : BaseViewModel() {
    fun finish() {
        viewModelScope.launch { tokenStore.setOnboardingDone() }
    }
}
