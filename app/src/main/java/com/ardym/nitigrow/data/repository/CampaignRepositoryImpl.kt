package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.realtime.RealtimeClient
import com.ardym.nitigrow.core.realtime.RealtimeEvent
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.local.dao.CampaignDao
import com.ardym.nitigrow.data.local.dao.TemplateDao
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.CampaignsApi
import com.ardym.nitigrow.data.remote.dto.AudienceEstimateRequest
import com.ardym.nitigrow.data.remote.dto.CreateCampaignRequest
import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.model.Template
import com.ardym.nitigrow.domain.repository.CampaignRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignRepositoryImpl @Inject constructor(
    private val api: CampaignsApi,
    private val dao: CampaignDao,
    private val templateDao: TemplateDao,
    private val realtime: RealtimeClient,
    private val dispatchers: DispatcherProvider
) : CampaignRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            realtime.events.collect { ev ->
                if (ev is RealtimeEvent.CampaignProgress) {
                    dao.setProgress(ev.campaignId, ev.sent, ev.delivered, ev.read, ev.failed)
                    dao.setStatus(ev.campaignId, ev.status.uppercase())
                }
            }
        }
    }

    override fun observeCampaigns(): Flow<List<Campaign>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeCampaign(id: String): Flow<Campaign?> =
        dao.observeOne(id).map { it?.toDomain() }

    override fun observeTemplates(): Flow<List<Template>> =
        templateDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshCampaigns(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> {
                dao.upsertAll(res.data.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun refreshTemplates(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.listTemplates() }) {
            is ApiResult.Success -> {
                templateDao.upsertAll(res.data.data.map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun estimateAudience(tags: List<String>): ApiResult<Int> =
        when (val res = safeApiCall(dispatchers.io) {
            api.estimate(AudienceEstimateRequest(tags))
        }) {
            is ApiResult.Success -> ApiResult.Success(res.data.count)
            is ApiResult.Error -> res
        }

    override suspend fun create(
        name: String,
        templateId: String,
        audienceTags: List<String>,
        scheduledAt: Instant?
    ): ApiResult<Campaign> =
        when (val res = safeApiCall(dispatchers.io) {
            api.create(
                CreateCampaignRequest(
                    name = name,
                    templateId = templateId,
                    audienceTags = audienceTags,
                    scheduledAt = scheduledAt?.toString()
                )
            )
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toEntity()
                dao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Error -> res
        }

    override suspend fun cancel(id: String): ApiResult<Unit> {
        dao.setStatus(id, "CANCELLED")  // optimistic
        return safeApiCall(dispatchers.io) { api.cancel(id); Unit }
    }
}
