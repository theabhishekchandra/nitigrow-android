package com.ardym.nitigrow.domain.usecase.dashboard

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.DashboardRepository
import javax.inject.Inject

class RefreshDashboardUseCase @Inject constructor(
    private val repo: DashboardRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
