package com.websbaba.nitigrow.domain.usecase.campaigns

import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTemplatesUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    operator fun invoke(): Flow<List<Template>> = repo.observeTemplates()
}
