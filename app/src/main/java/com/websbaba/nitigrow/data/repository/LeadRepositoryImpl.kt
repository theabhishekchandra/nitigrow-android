package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.LeadDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.LeadsApi
import com.websbaba.nitigrow.data.remote.dto.CreateLeadRequest
import com.websbaba.nitigrow.data.remote.dto.MoveLeadStageRequest
import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage
import com.websbaba.nitigrow.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadRepositoryImpl @Inject constructor(
    private val api: LeadsApi,
    private val dao: LeadDao,
    private val dispatchers: DispatcherProvider
) : LeadRepository {

    override fun observeLeads(): Flow<List<Lead>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> {
                dao.upsertAll((res.data.data ?: emptyList()).map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun moveStage(leadId: String, stage: LeadStage): ApiResult<Unit> {
        val now = System.currentTimeMillis()
        dao.setStage(leadId, stage.name, now)  // optimistic
        return safeApiCall(dispatchers.io) {
            api.moveStage(leadId, MoveLeadStageRequest(stage.name)); Unit
        }
    }

    override suspend fun create(
        contactId: String,
        source: String,
        stage: LeadStage,
        valueInr: Long,
        notes: String?
    ): ApiResult<Lead> =
        when (val res = safeApiCall(dispatchers.io) {
            api.create(CreateLeadRequest(contactId, source, stage.name, valueInr, notes))
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toEntity()
                dao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Error -> res
        }
}
