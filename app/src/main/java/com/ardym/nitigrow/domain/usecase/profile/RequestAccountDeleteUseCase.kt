package com.ardym.nitigrow.domain.usecase.profile

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * DPDP Act 2023 compliant. Server schedules deletion within 30 days,
 * sends confirmation email, allows undo within 7 days.
 */
class RequestAccountDeleteUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(reason: String?): ApiResult<Unit> =
        repo.requestAccountDelete(reason?.trim()?.takeIf { it.isNotBlank() })
}
