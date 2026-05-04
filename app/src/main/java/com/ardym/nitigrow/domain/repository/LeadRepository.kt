package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import kotlinx.coroutines.flow.Flow

interface LeadRepository {
    fun observeLeads(): Flow<List<Lead>>
    suspend fun refresh(): ApiResult<Unit>
    suspend fun moveStage(leadId: String, stage: LeadStage): ApiResult<Unit>
    suspend fun create(
        contactId: String,
        source: String,
        stage: LeadStage,
        valueInr: Long,
        notes: String?
    ): ApiResult<Lead>
}
