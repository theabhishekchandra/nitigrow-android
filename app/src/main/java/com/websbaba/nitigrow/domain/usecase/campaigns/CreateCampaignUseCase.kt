package com.websbaba.nitigrow.domain.usecase.campaigns

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.repository.CampaignRepository
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
        val created = repo.create(name.trim(), templateId, audienceTags, scheduledAt)
        if (created !is ApiResult.Success) return created

        // Creating a campaign never sends it — only a *scheduled* one is auto-enqueued by
        // the backend. "Send now" means no scheduledAt, so without this call it would sit
        // as an unsent draft forever with no error or indication anything is wrong.
        if (scheduledAt == null) {
            when (val launched = repo.launch(created.data.id)) {
                is ApiResult.Error -> return ApiResult.Error(
                    message = "Saved as a draft, but couldn't send: ${launched.message}",
                    code = launched.code,
                    type = launched.type
                )
                is ApiResult.Success -> Unit
            }
        }
        return created
    }
}
