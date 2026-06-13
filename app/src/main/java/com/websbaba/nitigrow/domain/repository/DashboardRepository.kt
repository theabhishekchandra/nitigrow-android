package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.DashboardStats
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    /** Cached stream — emits null when no cache yet. */
    fun observeStats(): Flow<DashboardStats?>
    /** Network refresh; on success, upserts Room (observers re-emit). */
    suspend fun refresh(): ApiResult<Unit>
}
