package com.ardym.nitigrow.domain.usecase.campaigns

import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCampaignUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    operator fun invoke(id: String): Flow<Campaign?> = repo.observeCampaign(id)
}
