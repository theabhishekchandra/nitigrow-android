package com.websbaba.nitigrow.presentation.feature.settings.waba

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.WabaStatus
import com.websbaba.nitigrow.domain.repository.SettingsRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WabaNumberEffect {
    data class Toast(val text: String) : WabaNumberEffect
}

@HiltViewModel
class WabaNumberViewModel @Inject constructor(
    private val settings: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(WabaNumberUiState())
    val state: StateFlow<WabaNumberUiState> = _state.asStateFlow()

    private val _effects = Channel<WabaNumberEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            when (val r = settings.get()) {
                is ApiResult.Success -> {
                    val s = r.data
                    _state.update {
                        it.copy(
                            phone = s.displayPhoneNumber.orEmpty(),
                            displayName = s.businessName,
                            status = if (s.whatsappConnected) WabaStatus.ACTIVE else WabaStatus.NOT_LINKED,
                            qualityRating = s.qualityRating,
                            messagingLimit = messagingLimitLabel(s.messagingTier),
                            dailyUsed = s.dailyMsgCount,
                            dailyLimit = s.dailyLimit
                        )
                    }
                }
                is ApiResult.Error -> _effects.send(WabaNumberEffect.Toast(r.message))
            }
        }
    }

    fun reverify() {
        // Re-verification is initiated from the Meta dashboard / embedded signup;
        // there's no standalone backend trigger, so we just acknowledge.
        viewModelScope.launch {
            _effects.send(WabaNumberEffect.Toast("Re-verification is managed from WhatsApp onboarding"))
        }
    }

    private fun messagingLimitLabel(tier: String): String = when (tier.uppercase()) {
        "TIER_10K" -> "10K/24h"
        "TIER_100K" -> "100K/24h"
        "TIER_UNLIMITED" -> "Unlimited"
        else -> "1K/24h"
    }
}
