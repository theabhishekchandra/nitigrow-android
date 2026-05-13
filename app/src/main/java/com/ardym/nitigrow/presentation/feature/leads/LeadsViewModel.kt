package com.ardym.nitigrow.presentation.feature.leads

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.domain.usecase.leads.MoveLeadStageUseCase
import com.ardym.nitigrow.domain.usecase.leads.ObserveLeadsUseCase
import com.ardym.nitigrow.domain.usecase.leads.RefreshLeadsUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import com.ardym.nitigrow.presentation.dummy.DummyData
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

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(LeadsUiState(leads = DummyData.leads()))
    val state: StateFlow<LeadsUiState> = _state.asStateFlow()

    init {
        observe()
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            // Guard keeps the seeded DummyData visible until the real Room flow has at least one row.
            .onEach { leads -> if (leads.isNotEmpty()) _state.update { it.copy(leads = leads) } }
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

    fun onMove(leadId: String, stage: LeadStage) {
        viewModelScope.launch { moveStage(leadId, stage) }
    }
}
