package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.AudienceEstimateRequest
import com.ardym.nitigrow.data.remote.dto.AudienceEstimateResponse
import com.ardym.nitigrow.data.remote.dto.CampaignDto
import com.ardym.nitigrow.data.remote.dto.CampaignListResponse
import com.ardym.nitigrow.data.remote.dto.CreateCampaignRequest
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.TemplateListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CampaignsApi {

    @GET("campaigns")
    suspend fun list(): CampaignListResponse

    @POST("campaigns")
    suspend fun create(@Body body: CreateCampaignRequest): CampaignDto

    @POST("campaigns/{id}/cancel")
    suspend fun cancel(@Path("id") id: String): GenericMessageDto

    @POST("campaigns/audience/estimate")
    suspend fun estimate(@Body body: AudienceEstimateRequest): AudienceEstimateResponse

    @GET("templates")
    suspend fun listTemplates(): TemplateListResponse
}
