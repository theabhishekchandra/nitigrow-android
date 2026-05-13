package com.ardym.nitigrow.domain.usecase.chat

import androidx.paging.PagingData
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PagedMessagesUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<PagingData<Message>> =
        repo.pagedMessages(conversationId)
}
