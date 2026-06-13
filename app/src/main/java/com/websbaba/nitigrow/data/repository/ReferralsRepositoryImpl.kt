package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.ReferralsApi
import com.websbaba.nitigrow.data.remote.dto.ReferralProgramDto
import com.websbaba.nitigrow.data.remote.dto.UpdateReferralProgramRequest
import com.websbaba.nitigrow.domain.model.LoyaltyProgram
import com.websbaba.nitigrow.domain.model.ReferralFunnel
import com.websbaba.nitigrow.domain.model.ReferralLeader
import com.websbaba.nitigrow.domain.model.ReferralProgram
import com.websbaba.nitigrow.domain.model.SaasReferral
import com.websbaba.nitigrow.domain.repository.ReferralsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralsRepositoryImpl @Inject constructor(
    private val api: ReferralsApi,
    private val dispatchers: DispatcherProvider
) : ReferralsRepository {

    private fun ReferralProgramDto.toDomain() = ReferralProgram(
        enabled = enabled,
        rewardType = rewardType,
        referrerReward = referrerReward,
        refereeReward = refereeReward,
        qualifyOn = qualifyOn,
        shareMessage = shareMessage.orEmpty()
    )

    override suspend fun getProgram(): ApiResult<ReferralProgram> =
        when (val r = safeApiCall(dispatchers.io) { api.getProgram() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toDomain())
            is ApiResult.Error -> r
        }

    override suspend fun setEnabled(enabled: Boolean): ApiResult<ReferralProgram> =
        when (val r = safeApiCall(dispatchers.io) {
            api.updateProgram(UpdateReferralProgramRequest(enabled))
        }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toDomain())
            is ApiResult.Error -> r
        }

    override suspend fun getFunnel(): ApiResult<ReferralFunnel> =
        when (val r = safeApiCall(dispatchers.io) { api.getStats() }) {
            is ApiResult.Success -> ApiResult.Success(
                ReferralFunnel(r.data.pending, r.data.qualified, r.data.rewarded, r.data.expired, r.data.total)
            )
            is ApiResult.Error -> r
        }

    override suspend fun getLeaderboard(): ApiResult<List<ReferralLeader>> =
        when (val r = safeApiCall(dispatchers.io) { api.getLeaderboard() }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.map { ReferralLeader(it.name, it.phone, it.rewarded) }
            )
            is ApiResult.Error -> r
        }

    override suspend fun getSaas(): ApiResult<SaasReferral> =
        when (val r = safeApiCall(dispatchers.io) { api.getSaas() }) {
            is ApiResult.Success -> ApiResult.Success(
                SaasReferral(
                    code = r.data.code,
                    signupLink = r.data.signupLink,
                    creditPaise = r.data.creditPaise,
                    signedUp = r.data.counts.signedUp,
                    converted = r.data.counts.credited,
                    creditPerReferralPaise = r.data.creditPerReferralPaise
                )
            )
            is ApiResult.Error -> r
        }

    override suspend fun getLoyalty(): ApiResult<LoyaltyProgram> =
        when (val r = safeApiCall(dispatchers.io) { api.getLoyalty() }) {
            is ApiResult.Success -> ApiResult.Success(
                LoyaltyProgram(r.data.enabled, r.data.pointsPerRupee, r.data.paisePerPoint, r.data.minRedeemPoints)
            )
            is ApiResult.Error -> r
        }
}
