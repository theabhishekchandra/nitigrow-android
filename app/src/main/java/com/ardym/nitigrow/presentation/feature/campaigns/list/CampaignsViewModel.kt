package com.ardym.nitigrow.presentation.feature.campaigns.list

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.repository.CampaignRepository
import com.ardym.nitigrow.domain.usecase.campaigns.ObserveCampaignsUseCase
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

data class CampaignsUiState(
    val items: List<Campaign> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    observe: ObserveCampaignsUseCase,
    private val repo: CampaignRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(CampaignsUiState())
    val state: StateFlow<CampaignsUiState> = _state.asStateFlow()

    init {
        observe()
            .onEach { items -> _state.update { it.copy(items = items) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = repo.refreshCampaigns()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }
}
