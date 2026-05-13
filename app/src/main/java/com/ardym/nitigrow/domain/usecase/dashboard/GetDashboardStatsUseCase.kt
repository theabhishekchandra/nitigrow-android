package com.ardym.nitigrow.domain.usecase.dashboard

import com.ardym.nitigrow.domain.model.DashboardStats
import com.ardym.nitigrow.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repo: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardStats?> = repo.observeStats()
}
