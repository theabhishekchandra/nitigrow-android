package com.websbaba.nitigrow.presentation.feature.referrals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ReferralsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralsViewModel @Inject constructor(
    private val repo: ReferralsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReferralsUiState())
    val state: StateFlow<ReferralsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val r = repo.getProgram()) {
                is ApiResult.Success -> _state.update { it.copy(program = r.data) }
                is ApiResult.Error -> _state.update { it.copy(error = r.message) }
            }
            (repo.getLoyalty() as? ApiResult.Success)?.let { r -> _state.update { it.copy(loyalty = r.data) } }
            (repo.getFunnel() as? ApiResult.Success)?.let { r -> _state.update { it.copy(funnel = r.data) } }
            (repo.getLeaderboard() as? ApiResult.Success)?.let { r -> _state.update { it.copy(leaders = r.data) } }
            (repo.getSaas() as? ApiResult.Success)?.let { r -> _state.update { it.copy(saas = r.data) } }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isToggling = true, error = null) }
            when (val r = repo.setEnabled(enabled)) {
                is ApiResult.Success -> _state.update { it.copy(program = r.data, isToggling = false) }
                is ApiResult.Error -> _state.update { it.copy(error = r.message, isToggling = false) }
            }
        }
    }
}
