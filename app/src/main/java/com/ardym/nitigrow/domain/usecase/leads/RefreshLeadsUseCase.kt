package com.ardym.nitigrow.domain.usecase.leads

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.LeadRepository
import javax.inject.Inject

class RefreshLeadsUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
