package com.websbaba.nitigrow.domain.usecase.leads

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.domain.repository.LeadRepository
import javax.inject.Inject

class MoveLeadStageUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    suspend operator fun invoke(leadId: String, stage: LeadStage): ApiResult<Unit> =
        repo.moveStage(leadId, stage)
}
