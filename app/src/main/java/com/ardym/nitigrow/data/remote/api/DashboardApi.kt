package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.AnalyticsOverviewDto
import com.ardym.nitigrow.data.remote.dto.DashboardStatsDto
import retrofit2.http.GET

interface DashboardApi {

    /**
     * Real backend route: `GET /api/analytics/overview`.
     * Returns the `{ stats, recentCampaigns }` envelope.
     */
    @GET("analytics/overview")
    suspend fun getOverview(): AnalyticsOverviewDto

    /**
     * Back-compat facade kept for [com.ardym.nitigrow.data.repository.DashboardRepositoryImpl],
     * which expects [DashboardStatsDto]. Default (non-abstract) so Retrofit ignores it and only
     * implements [getOverview]; the conversion lives in [AnalyticsOverviewDto.toStats].
     */
    suspend fun getStats(): DashboardStatsDto = getOverview().toStats()
}
