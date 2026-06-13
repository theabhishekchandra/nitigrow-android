package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.LoyaltyProgram
import com.websbaba.nitigrow.domain.model.ReferralFunnel
import com.websbaba.nitigrow.domain.model.ReferralLeader
import com.websbaba.nitigrow.domain.model.ReferralProgram
import com.websbaba.nitigrow.domain.model.SaasReferral

/**
 * Referrals + loyalty (Phase 10 H1–H3). Config + stats only, so no Room cache —
 * methods return ApiResult<domain> directly.
 */
interface ReferralsRepository {
    suspend fun getProgram(): ApiResult<ReferralProgram>
    suspend fun setEnabled(enabled: Boolean): ApiResult<ReferralProgram>
    suspend fun getFunnel(): ApiResult<ReferralFunnel>
    suspend fun getLeaderboard(): ApiResult<List<ReferralLeader>>
    suspend fun getSaas(): ApiResult<SaasReferral>
    suspend fun getLoyalty(): ApiResult<LoyaltyProgram>
}
