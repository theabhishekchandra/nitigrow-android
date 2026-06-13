package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.MessagesPerDayDto
import com.websbaba.nitigrow.data.remote.dto.TopAgentsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsApi {

    @GET("analytics/messages-per-day")
    suspend fun messagesPerDay(
        @Query("from") from: String?,
        @Query("to") to: String?
    ): List<MessagesPerDayDto>

    @GET("analytics/top-agents")
    suspend fun topAgents(): TopAgentsResponseDto
}
