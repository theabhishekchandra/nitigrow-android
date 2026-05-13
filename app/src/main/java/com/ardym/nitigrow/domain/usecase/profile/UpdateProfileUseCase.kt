package com.ardym.nitigrow.domain.usecase.profile

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

class UpdateProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(name: String, email: String): ApiResult<User> {
        if (name.isBlank()) return ApiResult.Error(message = "Name required")
        if (!EMAIL_REGEX.matches(email)) return ApiResult.Error(message = "Invalid email")
        return repo.updateProfile(name.trim(), email.trim())
    }
}
