package com.ardym.nitigrow.domain.usecase.contacts

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.ContactRepository
import javax.inject.Inject

class RefreshContactsUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
