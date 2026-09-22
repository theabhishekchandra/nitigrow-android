package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.Template
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CampaignRepository {
    fun observeCampaigns(): Flow<List<Campaign>>
    fun observeCampaign(id: String): Flow<Campaign?>
    fun observeTemplates(): Flow<List<Template>>

    suspend fun refreshCampaigns(): ApiResult<Unit>
    suspend fun refreshTemplates(): ApiResult<Unit>

    suspend fun estimateAudience(tags: List<String>): ApiResult<Int>

    suspend fun create(
        name: String,
        templateId: String,
        audienceTags: List<String>,
        scheduledAt: Instant?
    ): ApiResult<Campaign>

    suspend fun cancel(id: String): ApiResult<Unit>

    /**
     * Starts sending a `draft`/`scheduled` campaign now. Creating a campaign never sends
     * it by itself — only a *scheduled* one is auto-enqueued by the backend — so a "send
     * now" campaign must call this right after [create] or it silently never sends.
     */
    suspend fun launch(id: String): ApiResult<Unit>
}
