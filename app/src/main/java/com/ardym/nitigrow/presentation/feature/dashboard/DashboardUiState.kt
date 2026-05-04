package com.ardym.nitigrow.presentation.feature.dashboard

import com.ardym.nitigrow.domain.model.DashboardStats

data class DashboardUiState(
    val stats: DashboardStats? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isInitialLoading: Boolean get() = stats == null && isRefreshing
}
