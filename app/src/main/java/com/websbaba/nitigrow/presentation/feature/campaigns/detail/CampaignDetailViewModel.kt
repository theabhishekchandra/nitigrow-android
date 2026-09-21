package com.websbaba.nitigrow.presentation.feature.campaigns.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import com.websbaba.nitigrow.domain.usecase.campaigns.CancelCampaignUseCase
import com.websbaba.nitigrow.domain.usecase.campaigns.ObserveCampaignUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CampaignDetailUiState(
    val campaign: Campaign? = null,
    /** The template the broadcast uses, when it is in the local cache. */
    val template: Template? = null,
    val isRefreshing: Boolean = false,
    val confirmingCancel: Boolean = false,
    val cancelling: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CampaignDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeCampaign: ObserveCampaignUseCase,
    private val repo: CampaignRepository,
    private val cancel: CancelCampaignUseCase
) : BaseViewModel() {

    private val campaignId: String = savedState.get<String>("campaignId").orEmpty()

    private val _state = MutableStateFlow(CampaignDetailUiState())
    val state: StateFlow<CampaignDetailUiState> = _state.asStateFlow()

    init {
        combine(observeCampaign(campaignId), repo.observeTemplates()) { campaign, templates ->
            campaign to templates.firstOrNull { it.id == campaign?.templateId }
        }
            .onEach { (c, template) ->
                if (c != null) _state.update { it.copy(campaign = c, template = template) }
            }
            .launchIn(viewModelScope)
    }

    /** Pull-to-refresh: re-fetches campaigns so a running broadcast's counts stay live. */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = repo.refreshCampaigns()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update { it.copy(isRefreshing = false, error = res.message) }
            }
        }
    }

    fun onCancelRequested() = _state.update { it.copy(confirmingCancel = true) }

    fun onCancelDismissed() = _state.update { it.copy(confirmingCancel = false) }

    fun onCancelConfirmed() {
        viewModelScope.launch {
            _state.update { it.copy(confirmingCancel = false, cancelling = true) }
            when (val res = cancel(campaignId)) {
                is ApiResult.Success -> _state.update { it.copy(cancelling = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(cancelling = false, error = res.message)
                }
            }
        }
    }
}
