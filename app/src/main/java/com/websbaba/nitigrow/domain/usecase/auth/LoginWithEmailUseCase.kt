package com.websbaba.nitigrow.domain.usecase.auth

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<User> {
        return repo.login(email, password)
    }
}
