package com.websbaba.nitigrow.domain.usecase.dashboard

import com.websbaba.nitigrow.domain.model.DashboardStats
import com.websbaba.nitigrow.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repo: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardStats?> = repo.observeStats()
}
