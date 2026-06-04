package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.LoyaltyProgram
import com.ardym.nitigrow.domain.model.ReferralFunnel
import com.ardym.nitigrow.domain.model.ReferralLeader
import com.ardym.nitigrow.domain.model.ReferralProgram
import com.ardym.nitigrow.domain.model.SaasReferral

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
