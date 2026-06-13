package com.websbaba.nitigrow.domain.usecase.auth

import com.websbaba.nitigrow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.observeAuthState()
}
