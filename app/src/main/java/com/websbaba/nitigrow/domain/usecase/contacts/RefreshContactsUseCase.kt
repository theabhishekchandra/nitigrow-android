package com.websbaba.nitigrow.domain.usecase.contacts

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ContactRepository
import javax.inject.Inject

class RefreshContactsUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
