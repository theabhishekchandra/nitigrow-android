package com.websbaba.nitigrow.domain.usecase.profile

import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<User?> = repo.observeProfile()
}
