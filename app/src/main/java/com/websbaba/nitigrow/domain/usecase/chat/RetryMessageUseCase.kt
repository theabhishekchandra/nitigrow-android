package com.websbaba.nitigrow.domain.usecase.chat

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ChatRepository
import javax.inject.Inject

class RetryMessageUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(clientId: String): ApiResult<Unit> = repo.retry(clientId)
}
