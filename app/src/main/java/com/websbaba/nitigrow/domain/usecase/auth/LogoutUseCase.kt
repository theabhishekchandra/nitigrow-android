package com.websbaba.nitigrow.domain.usecase.auth

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.data.local.dao.ConversationDao
import com.websbaba.nitigrow.data.local.dao.MessageDao
import com.websbaba.nitigrow.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Logs the user out and purges tenant-scoped local data.
 *
 * Multi-tenant correctness: the message/conversation cache is keyed per tenant,
 * but logging out is also the only way to switch tenants in this app (a new
 * session re-authenticates). We therefore wipe the chat caches here so a second
 * tenant signing in on the same device can never see the first tenant's
 * conversations or messages. The wipe runs before the token store is cleared by
 * the repository, and even if the network logout call fails the local tables are
 * still cleared.
 */
class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        // Purge tenant-scoped caches first so stale rows can't survive a failed
        // network call or a fast re-login on the same device.
        messageDao.clear()
        conversationDao.clear()
        return repo.logout()
    }
}
