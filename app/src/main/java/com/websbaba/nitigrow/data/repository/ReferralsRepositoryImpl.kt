package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
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
        safeApiCall(dispatchers.io) { api.getProgram() }.andThen { r ->
            ApiResult.Success(r.toDomain())
        }

    override suspend fun setEnabled(enabled: Boolean): ApiResult<ReferralProgram> =
        safeApiCall(dispatchers.io) {
            api.updateProgram(UpdateReferralProgramRequest(enabled))
        }.andThen { r ->
            ApiResult.Success(r.toDomain())
        }

    override suspend fun getFunnel(): ApiResult<ReferralFunnel> =
        safeApiCall(dispatchers.io) { api.getStats() }.andThen { r ->
            ApiResult.Success(
                ReferralFunnel(r.pending, r.qualified, r.rewarded, r.expired, r.total)
            )
        }

    override suspend fun getLeaderboard(): ApiResult<List<ReferralLeader>> =
        safeApiCall(dispatchers.io) { api.getLeaderboard() }.andThen { r ->
            ApiResult.Success(
                r.map { ReferralLeader(it.name, it.phone, it.rewarded) }
            )
        }

    override suspend fun getSaas(): ApiResult<SaasReferral> =
        safeApiCall(dispatchers.io) { api.getSaas() }.andThen { r ->
            ApiResult.Success(
                SaasReferral(
                    code = r.code,
                    signupLink = r.signupLink,
                    creditPaise = r.creditPaise,
                    signedUp = r.counts.signedUp,
                    converted = r.counts.credited,
                    creditPerReferralPaise = r.creditPerReferralPaise
                )
            )
        }

    override suspend fun getLoyalty(): ApiResult<LoyaltyProgram> =
        safeApiCall(dispatchers.io) { api.getLoyalty() }.andThen { r ->
            ApiResult.Success(
                LoyaltyProgram(r.enabled, r.pointsPerRupee, r.paisePerPoint, r.minRedeemPoints)
            )
        }
}
