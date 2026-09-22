package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.AnalyticsOverviewDto
import retrofit2.http.GET

interface DashboardApi {

    /**
     * Real backend route: `GET /api/analytics/overview`.
     * Returns the `{ stats, recentCampaigns }` envelope; convert with [AnalyticsOverviewDto.toStats].
     * Retrofit interfaces must stay abstract-only: a default method here made the release build
     * crash with a ClassCastException when Hilt created the API.
     */
    @GET("analytics/overview")
    suspend fun getOverview(): AnalyticsOverviewDto
}
