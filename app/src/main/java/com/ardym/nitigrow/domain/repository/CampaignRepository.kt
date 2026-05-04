package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.model.Template
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
}
