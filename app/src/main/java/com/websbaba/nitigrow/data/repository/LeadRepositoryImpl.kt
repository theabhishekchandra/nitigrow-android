package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
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
        safeApiCall(dispatchers.io) { api.list() }.andThen { res ->
            dao.upsertAll((res.data ?: emptyList()).map { it.toEntity() })
            ApiResult.Success(Unit)
        }

    override suspend fun moveStage(leadId: String, stage: LeadStage): ApiResult<Unit> {
        val previousStage = dao.get(leadId)?.stage
        val now = System.currentTimeMillis()
        // Local column stores the enum's own name (see LeadMapper); the wire body
        // needs the backend's 5-value vocabulary — see LeadStage.toBackend().
        dao.setStage(leadId, stage.name, now)  // optimistic
        val result = safeApiCall(dispatchers.io) {
            api.moveStage(leadId, MoveLeadStageRequest(stage.toBackend())); Unit
        }
        // A rejected move (offline, permission, a stage the backend refuses) must not leave
        // the card sitting on a column the server never agreed to — put it back so the
        // board reflects reality instead of a card that silently snaps back on next refresh.
        if (result is ApiResult.Error && previousStage != null) {
            dao.setStage(leadId, previousStage, now)
        }
        return result
    }

    override suspend fun create(
        contactId: String,
        name: String,
        source: String,
        stage: LeadStage,
        valueInr: Long,
        notes: String?
    ): ApiResult<Lead> =
        safeApiCall(dispatchers.io) {
            api.create(CreateLeadRequest(contactId, name, source, stage.toBackend(), valueInr, notes))
        }.andThen { res ->
            val entity = res.toEntity()
            dao.upsert(entity)
            ApiResult.Success(entity.toDomain())
        }
}
