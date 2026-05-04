package com.ardym.nitigrow.domain.usecase.auth

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.logout()
}
