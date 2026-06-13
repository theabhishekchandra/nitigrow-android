package com.websbaba.nitigrow.domain.usecase.campaigns

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import javax.inject.Inject

class CancelCampaignUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repo.cancel(id)
}
