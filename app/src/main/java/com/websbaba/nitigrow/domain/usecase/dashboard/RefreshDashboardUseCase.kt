package com.websbaba.nitigrow.domain.usecase.dashboard

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.DashboardRepository
import javax.inject.Inject

class RefreshDashboardUseCase @Inject constructor(
    private val repo: DashboardRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
