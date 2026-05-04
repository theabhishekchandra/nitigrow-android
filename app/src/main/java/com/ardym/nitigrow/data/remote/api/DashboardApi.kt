package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.DashboardStatsDto
import retrofit2.http.GET

interface DashboardApi {
    @GET("dashboard/stats")
    suspend fun getStats(): DashboardStatsDto
}
