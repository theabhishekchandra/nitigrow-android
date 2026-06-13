package com.websbaba.nitigrow.domain.usecase.auth

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCases are thin orchestrators. One public `invoke`. Easy to unit-test.
 * Add input validation here so ViewModels stay dumb.
 */
class RequestOtpUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(phone: String): ApiResult<Unit> {
        val cleaned = phone.filter { it.isDigit() }
        if (cleaned.length !in 10..13) {
            return ApiResult.Error(message = "Invalid phone number")
        }
        return repo.requestOtp(cleaned)
    }
}
