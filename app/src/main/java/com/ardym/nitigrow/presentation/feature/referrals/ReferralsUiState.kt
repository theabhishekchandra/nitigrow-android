package com.ardym.nitigrow.presentation.feature.referrals

import com.ardym.nitigrow.domain.model.LoyaltyProgram
import com.ardym.nitigrow.domain.model.ReferralFunnel
import com.ardym.nitigrow.domain.model.ReferralLeader
import com.ardym.nitigrow.domain.model.ReferralProgram
import com.ardym.nitigrow.domain.model.SaasReferral

data class ReferralsUiState(
    val isLoading: Boolean = true,
    val isToggling: Boolean = false,
    val program: ReferralProgram? = null,
    val loyalty: LoyaltyProgram? = null,
    val funnel: ReferralFunnel? = null,
    val leaders: List<ReferralLeader> = emptyList(),
    val saas: SaasReferral? = null,
    val error: String? = null
)
