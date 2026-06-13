package com.websbaba.nitigrow.domain.usecase.campaigns

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import javax.inject.Inject

class EstimateAudienceUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    suspend operator fun invoke(tags: List<String>): ApiResult<Int> =
        if (tags.isEmpty()) ApiResult.Success(0) else repo.estimateAudience(tags)
}
