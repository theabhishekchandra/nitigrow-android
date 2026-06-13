package com.websbaba.nitigrow.domain.usecase.campaigns

import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCampaignsUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    operator fun invoke(): Flow<List<Campaign>> = repo.observeCampaigns()
}
