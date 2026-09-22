package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.realtime.RealtimeClient
import com.websbaba.nitigrow.core.realtime.RealtimeEvent
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.CampaignDao
import com.websbaba.nitigrow.data.local.dao.TemplateDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.CampaignsApi
import com.websbaba.nitigrow.data.remote.dto.AudienceEstimateRequest
import com.websbaba.nitigrow.data.remote.dto.CampaignAudienceRequest
import com.websbaba.nitigrow.data.remote.dto.CreateCampaignRequest
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.domain.repository.CampaignRepository
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
        safeApiCall(dispatchers.io) { api.list() }.andThen { res ->
            dao.upsertAll((res.data ?: emptyList()).map { it.toEntity() })
            ApiResult.Success(Unit)
        }

    override suspend fun refreshTemplates(): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.listTemplates() }.andThen { res ->
            templateDao.upsertAll(res.map { it.toEntity() })
            ApiResult.Success(Unit)
        }

    override suspend fun estimateAudience(tags: List<String>): ApiResult<Int> =
        safeApiCall(dispatchers.io) {
            api.estimate(AudienceEstimateRequest(tags))
        }.andThen { res ->
            ApiResult.Success(res.count)
        }

    override suspend fun create(
        name: String,
        templateId: String,
        audienceTags: List<String>,
        scheduledAt: Instant?
    ): ApiResult<Campaign> =
        safeApiCall(dispatchers.io) {
            api.create(
                CreateCampaignRequest(
                    name = name,
                    templateId = templateId,
                    // The backend's Joi schema requires this nested shape and silently
                    // drops any other top-level key — a flat `audienceTags` field was
                    // ignored, so every campaign defaulted to audience "all" instead of
                    // the tags picked here.
                    audience = CampaignAudienceRequest(type = "tag", tags = audienceTags),
                    scheduledAt = scheduledAt?.toString()
                )
            )
        }.andThen { res ->
            val entity = res.toEntity()
            dao.upsert(entity)
            ApiResult.Success(entity.toDomain())
        }

    override suspend fun cancel(id: String): ApiResult<Unit> {
        dao.setStatus(id, "CANCELLED")  // optimistic
        return safeApiCall(dispatchers.io) { api.cancel(id); Unit }
    }

    override suspend fun launch(id: String): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.launch(id) }.andThen {
            dao.setStatus(id, "RUNNING")
            ApiResult.Success(Unit)
        }
}
