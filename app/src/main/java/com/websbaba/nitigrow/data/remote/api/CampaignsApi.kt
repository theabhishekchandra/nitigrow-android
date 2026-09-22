package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.AudienceEstimateRequest
import com.websbaba.nitigrow.data.remote.dto.AudienceEstimateResponse
import com.websbaba.nitigrow.data.remote.dto.CampaignDto
import com.websbaba.nitigrow.data.remote.dto.CampaignListResponse
import com.websbaba.nitigrow.data.remote.dto.CreateCampaignRequest
import com.websbaba.nitigrow.data.remote.dto.GenericMessageDto
import com.websbaba.nitigrow.data.remote.dto.TemplateDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CampaignsApi {

    @GET("campaigns")
    suspend fun list(): CampaignListResponse

    @POST("campaigns")
    suspend fun create(@Body body: CreateCampaignRequest): CampaignDto

    // Creating a campaign only ever leaves it `draft` (or `scheduled`, if scheduledAt was
    // given — the backend enqueues that one itself). A "send now" campaign has no
    // scheduledAt, so it stays a draft forever unless this is called right after create.
    // Backend responds `{ message, total }`, not the campaign document.
    @POST("campaigns/{id}/launch")
    suspend fun launch(@Path("id") id: String): GenericMessageDto

    @POST("campaigns/{id}/cancel")
    suspend fun cancel(@Path("id") id: String): GenericMessageDto

    @POST("campaigns/audience/estimate")
    suspend fun estimate(@Body body: AudienceEstimateRequest): AudienceEstimateResponse

    // GET /templates returns a top-level array, not a wrapped object.
    @GET("templates")
    suspend fun listTemplates(): List<TemplateDto>
}
