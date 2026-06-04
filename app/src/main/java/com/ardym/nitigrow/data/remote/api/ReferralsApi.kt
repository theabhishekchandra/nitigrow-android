package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.LoyaltyProgramDto
import com.ardym.nitigrow.data.remote.dto.ReferralLeaderDto
import com.ardym.nitigrow.data.remote.dto.ReferralProgramDto
import com.ardym.nitigrow.data.remote.dto.ReferralStatsDto
import com.ardym.nitigrow.data.remote.dto.SaasReferralDto
import com.ardym.nitigrow.data.remote.dto.UpdateReferralProgramRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ReferralsApi {

    @GET("referrals/program")
    suspend fun getProgram(): ReferralProgramDto

    @PUT("referrals/program")
    suspend fun updateProgram(@Body body: UpdateReferralProgramRequest): ReferralProgramDto

    @GET("referrals/stats")
    suspend fun getStats(): ReferralStatsDto

    @GET("referrals/leaderboard")
    suspend fun getLeaderboard(): List<ReferralLeaderDto>

    @GET("referrals/saas")
    suspend fun getSaas(): SaasReferralDto

    @GET("loyalty/program")
    suspend fun getLoyalty(): LoyaltyProgramDto
}
