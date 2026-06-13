package com.websbaba.nitigrow.domain.usecase.leads

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.LeadRepository
import javax.inject.Inject

class RefreshLeadsUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
