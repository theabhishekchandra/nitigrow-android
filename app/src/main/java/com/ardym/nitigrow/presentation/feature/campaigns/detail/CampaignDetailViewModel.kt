package com.ardym.nitigrow.presentation.feature.campaigns.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.usecase.campaigns.CancelCampaignUseCase
import com.ardym.nitigrow.domain.usecase.campaigns.ObserveCampaignUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CampaignDetailUiState(
    val campaign: Campaign? = null,
    val cancelling: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CampaignDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeCampaign: ObserveCampaignUseCase,
    private val cancel: CancelCampaignUseCase
) : BaseViewModel() {

    private val campaignId: String = savedState.get<String>("campaignId").orEmpty()

    private val _state = MutableStateFlow(CampaignDetailUiState())
    val state: StateFlow<CampaignDetailUiState> = _state.asStateFlow()

    init {
        observeCampaign(campaignId)
            .onEach { c -> _state.update { it.copy(campaign = c) } }
            .launchIn(viewModelScope)
    }

    fun onCancel() {
        viewModelScope.launch {
            _state.update { it.copy(cancelling = true) }
            when (val res = cancel(campaignId)) {
                is ApiResult.Success -> _state.update { it.copy(cancelling = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(cancelling = false, error = res.message)
                }
            }
        }
    }
}
