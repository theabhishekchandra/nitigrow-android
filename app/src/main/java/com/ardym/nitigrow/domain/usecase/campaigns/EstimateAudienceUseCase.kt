package com.ardym.nitigrow.domain.usecase.campaigns

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.CampaignRepository
import javax.inject.Inject

class EstimateAudienceUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    suspend operator fun invoke(tags: List<String>): ApiResult<Int> =
        if (tags.isEmpty()) ApiResult.Success(0) else repo.estimateAudience(tags)
}
