package com.websbaba.nitigrow.domain.usecase.inbox

import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase @Inject constructor(
    private val repo: InboxRepository
) {
    operator fun invoke(query: String): Flow<List<Conversation>> =
        repo.observeConversations(query.trim())
}
