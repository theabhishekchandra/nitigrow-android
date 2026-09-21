package com.websbaba.nitigrow.presentation.feature.campaigns.list

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import com.websbaba.nitigrow.domain.usecase.campaigns.ObserveCampaignsUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    observe: ObserveCampaignsUseCase,
    private val repo: CampaignRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(CampaignsUiState(isRefreshing = true))
    val state: StateFlow<CampaignsUiState> = _state.asStateFlow()

    init {
        observe()
            .onEach { items -> _state.update { it.copy(items = items) } }
            .launchIn(viewModelScope)
        repo.observeTemplates()
            .onEach { templates ->
                _state.update {
                    it.copy(approvedTemplates = templates.count { t -> t.status.equals("APPROVED", ignoreCase = true) })
                }
            }
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
        // Best-effort: keeps the "N approved" hint on the Templates shortcut fresh.
        viewModelScope.launch { repo.refreshTemplates() }
    }

    fun onFilterChange(filter: BroadcastFilter) = _state.update { it.copy(filter = filter) }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    /** Shows or hides the search field; hiding also clears the query. */
    fun onSearchToggle() = _state.update {
        if (it.isSearching) it.copy(isSearching = false, query = "") else it.copy(isSearching = true)
    }
}
