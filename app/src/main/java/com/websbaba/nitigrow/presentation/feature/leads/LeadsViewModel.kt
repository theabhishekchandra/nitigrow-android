package com.websbaba.nitigrow.presentation.feature.leads

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.domain.usecase.leads.MoveLeadStageUseCase
import com.websbaba.nitigrow.domain.usecase.leads.ObserveLeadsUseCase
import com.websbaba.nitigrow.domain.usecase.leads.RefreshLeadsUseCase
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
class LeadsViewModel @Inject constructor(
    observe: ObserveLeadsUseCase,
    private val refreshLeads: RefreshLeadsUseCase,
    private val moveStage: MoveLeadStageUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(LeadsUiState(isRefreshing = true))
    val state: StateFlow<LeadsUiState> = _state.asStateFlow()

    init {
        observe()
            .onEach { leads -> _state.update { it.copy(leads = leads) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = refreshLeads()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun onStageFilter(stage: LeadStage?) = _state.update { it.copy(stageFilter = stage) }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    /** Shows or hides the search field; hiding also clears the query. */
    fun onSearchToggle() = _state.update {
        if (it.isSearching) it.copy(isSearching = false, query = "") else it.copy(isSearching = true)
    }

    fun onMove(leadId: String, stage: LeadStage) {
        viewModelScope.launch { moveStage(leadId, stage) }
    }
}
