package com.ardym.nitigrow.domain.usecase.chat

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.ChatRepository
import javax.inject.Inject

class RetryMessageUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(clientId: String): ApiResult<Unit> = repo.retry(clientId)
}
