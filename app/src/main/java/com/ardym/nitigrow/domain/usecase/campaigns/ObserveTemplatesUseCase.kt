package com.ardym.nitigrow.domain.usecase.campaigns

import com.ardym.nitigrow.domain.model.Template
import com.ardym.nitigrow.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTemplatesUseCase @Inject constructor(
    private val repo: CampaignRepository
) {
    operator fun invoke(): Flow<List<Template>> = repo.observeTemplates()
}
