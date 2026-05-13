package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.DashboardStats
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    /** Cached stream — emits null when no cache yet. */
    fun observeStats(): Flow<DashboardStats?>
    /** Network refresh; on success, upserts Room (observers re-emit). */
    suspend fun refresh(): ApiResult<Unit>
}
