package com.websbaba.nitigrow.domain.usecase.profile

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * DPDP Act 2023 right to erasure. Owner-only and password-confirmed: the server
 * marks the tenant cancelled and permanently erases all data within 30 days.
 */
class RequestAccountDeleteUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(password: String): ApiResult<Unit> =
        repo.requestAccountDelete(password)
}
