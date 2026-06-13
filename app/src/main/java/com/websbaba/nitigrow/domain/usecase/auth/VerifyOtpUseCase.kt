package com.websbaba.nitigrow.domain.usecase.auth

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(phone: String, code: String): ApiResult<User> {
        if (code.length != OTP_LEN || !code.all { it.isDigit() }) {
            return ApiResult.Error(message = "Enter the 6-digit code")
        }
        return repo.verifyOtp(phone, code)
    }

    companion object { const val OTP_LEN = 6 }
}
