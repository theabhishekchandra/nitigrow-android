package com.ardym.nitigrow.domain.usecase.inbox

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.InboxRepository
import javax.inject.Inject

class RefreshInboxUseCase @Inject constructor(
    private val repo: InboxRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
