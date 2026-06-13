package com.websbaba.nitigrow.presentation.feature.leads.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.domain.repository.LeadRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the single-lead detail screen.
 *
 * @param lead the lead being viewed, or null until it resolves from cache/network.
 * @param isRefreshing true while a backing /leads fetch is in flight.
 * @param movingStage true while a PATCH /leads/:id/stage is in flight.
 * @param error user-facing error message from the last failed operation, if any.
 */
data class LeadDetailUiState(
    val lead: Lead? = null,
    val isRefreshing: Boolean = false,
    val movingStage: Boolean = false,
    val error: String? = null
)

/**
 * Drives [LeadDetailScreen]. Reads the `leadId` nav argument, observes the lead
 * out of the local cache (kept warm by the leads repository), and lets the user
 * advance the pipeline stage — wired to PATCH /api/leads/:id/stage via
 * [LeadRepository.moveStage]. A refresh is kicked off on init so a deep-linked
 * lead that isn't cached yet is fetched from GET /api/leads.
 */
@HiltViewModel
class LeadDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: LeadRepository
) : BaseViewModel() {

    private val leadId: String = savedState.get<String>(ARG_LEAD_ID).orEmpty()

    private val _state = MutableStateFlow(LeadDetailUiState(isRefreshing = true))
    val state: StateFlow<LeadDetailUiState> = _state.asStateFlow()

    init {
        repository.observeLeads()
            .map { leads -> leads.firstOrNull { it.id == leadId } }
            .onEach { lead -> _state.update { it.copy(lead = lead) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = repository.refresh()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun onMoveStage(stage: LeadStage) {
        val current = _state.value.lead ?: return
        if (current.stage == stage) return
        viewModelScope.launch {
            _state.update { it.copy(movingStage = true, error = null) }
            when (val res = repository.moveStage(current.id, stage)) {
                is ApiResult.Success -> _state.update { it.copy(movingStage = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(movingStage = false, error = res.message)
                }
            }
        }
    }

    companion object {
        const val ARG_LEAD_ID = "leadId"
    }
}
