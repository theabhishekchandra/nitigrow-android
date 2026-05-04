package com.ardym.nitigrow.domain.usecase.leads

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.domain.repository.LeadRepository
import javax.inject.Inject

class MoveLeadStageUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    suspend operator fun invoke(leadId: String, stage: LeadStage): ApiResult<Unit> =
        repo.moveStage(leadId, stage)
}
