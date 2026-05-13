package com.ardym.nitigrow.domain.usecase.campaigns

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.CampaignRepository
import javax.inject.Inject

class CancelCampaignUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repo.cancel(id)
}
