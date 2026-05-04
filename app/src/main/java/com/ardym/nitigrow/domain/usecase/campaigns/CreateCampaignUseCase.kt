package com.ardym.nitigrow.domain.usecase.campaigns

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.repository.CampaignRepository
import java.time.Instant
import javax.inject.Inject

class CreateCampaignUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    suspend operator fun invoke(
        name: String,
        templateId: String,
        audienceTags: List<String>,
        scheduledAt: Instant?
    ): ApiResult<Campaign> {
        if (name.isBlank()) return ApiResult.Error(message = "Name required")
        if (templateId.isBlank()) return ApiResult.Error(message = "Pick a template")
        if (audienceTags.isEmpty()) return ApiResult.Error(message = "Pick at least one audience tag")
        if (scheduledAt != null && scheduledAt.isBefore(Instant.now().plusSeconds(60))) {
            return ApiResult.Error(message = "Schedule at least 1 minute ahead")
        }
        return repo.create(name.trim(), templateId, audienceTags, scheduledAt)
    }
}
