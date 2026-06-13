package com.websbaba.nitigrow.domain.usecase.inbox

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.InboxRepository
import javax.inject.Inject

class RefreshInboxUseCase @Inject constructor(
    private val repo: InboxRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = repo.refresh()
}
