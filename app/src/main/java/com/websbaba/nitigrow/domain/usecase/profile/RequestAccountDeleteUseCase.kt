package com.websbaba.nitigrow.domain.usecase.profile

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ProfileRepository
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
