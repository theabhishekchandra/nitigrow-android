package com.ardym.nitigrow.domain.usecase.inbox

import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase @Inject constructor(
    private val repo: InboxRepository
) {
    operator fun invoke(query: String): Flow<List<Conversation>> =
        repo.observeConversations(query.trim())
}
