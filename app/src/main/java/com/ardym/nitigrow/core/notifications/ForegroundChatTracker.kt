package com.ardym.nitigrow.core.notifications

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that ChatScreen sets when entered, clears when exited.
 * NotificationService consults to suppress notif for currently-open conversation.
 */
@Singleton
class ForegroundChatTracker @Inject constructor() {
    @Volatile private var openConversationId: String? = null

    fun enter(conversationId: String) { openConversationId = conversationId }
    fun exit(conversationId: String) {
        if (openConversationId == conversationId) openConversationId = null
    }
    fun isOpen(conversationId: String): Boolean = openConversationId == conversationId
}
