package com.ardym.nitigrow.presentation.feature.dashboard

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.ardym.nitigrow.domain.usecase.dashboard.RefreshDashboardUseCase
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
class DashboardViewModel @Inject constructor(
    getStats: GetDashboardStatsUseCase,
    private val refreshStats: RefreshDashboardUseCase
) : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(DashboardUiState(stats = DummyData.dashboardStats()))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        getStats()
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            // Guard keeps the seeded DummyData visible until the real API responds with non-null stats.
            .onEach { stats -> if (stats != null) _state.update { it.copy(stats = stats) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = refreshStats()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
