package com.websbaba.nitigrow.presentation.feature.flows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.WaFlow
import com.websbaba.nitigrow.domain.repository.FlowsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlowsViewModel @Inject constructor(
    private val repo: FlowsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FlowsUiState())
    val state: StateFlow<FlowsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val r = repo.getFlows()) {
                is ApiResult.Success -> _state.update { it.copy(flows = r.data, isLoading = false) }
                is ApiResult.Error -> _state.update { it.copy(error = r.message, isLoading = false) }
            }
        }
    }

    fun sendFlow(flowId: String, to: String, body: String?) {
        viewModelScope.launch {
            _state.update { it.copy(sending = true, error = null, sentMessage = null) }
            when (val r = repo.sendFlow(flowId, to, body)) {
                is ApiResult.Success -> _state.update { it.copy(sending = false, sentMessage = "Flow sent") }
                is ApiResult.Error -> _state.update { it.copy(sending = false, error = r.message) }
            }
        }
    }

    fun openSubmissions(flow: WaFlow) {
        _state.update { it.copy(submissionsFor = flow, submissions = emptyList(), loadingSubmissions = true) }
        viewModelScope.launch {
            when (val r = repo.getSubmissions(flow.id)) {
                is ApiResult.Success -> _state.update { it.copy(submissions = r.data, loadingSubmissions = false) }
                is ApiResult.Error -> _state.update { it.copy(error = r.message, loadingSubmissions = false) }
            }
        }
    }

    fun closeSubmissions() = _state.update { it.copy(submissionsFor = null, submissions = emptyList()) }
    fun clearSent() = _state.update { it.copy(sentMessage = null) }
}
