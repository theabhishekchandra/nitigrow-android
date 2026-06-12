package com.ardym.nitigrow.domain.usecase.auth

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<User> {
        return repo.login(email, password)
    }
}
