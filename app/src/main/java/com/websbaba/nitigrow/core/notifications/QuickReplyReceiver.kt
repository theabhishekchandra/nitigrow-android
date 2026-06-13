package com.websbaba.nitigrow.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.websbaba.nitigrow.domain.usecase.chat.MarkConversationReadUseCase
import com.websbaba.nitigrow.domain.usecase.chat.SendMessageUseCase
import com.websbaba.nitigrow.work.PendingMessageScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives the inline notification actions wired by [NotificationActionsHelper].
 *
 * - [ACTION_REPLY] routes the RemoteInput text through [SendMessageUseCase] (which
 *   performs an optimistic local insert + background send) and then enqueues
 *   [PendingMessageScheduler] so the reply survives process death / transient
 *   network failure via WorkManager.
 * - [ACTION_MARK_READ] clears the unread count locally and calls
 *   `PATCH messages/{contactId}/read` through [MarkConversationReadUseCase].
 *
 * Hilt injects the use cases directly into the receiver. Because both side-effects
 * are suspending, the receiver holds the broadcast alive with [goAsync] and only
 * calls `finish()` once the work completes.
 */
@AndroidEntryPoint
class QuickReplyReceiver : BroadcastReceiver() {

    @Inject lateinit var sendMessage: SendMessageUseCase

    @Inject lateinit var markConversationRead: MarkConversationReadUseCase

    @Inject lateinit var pendingMessageScheduler: PendingMessageScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID).orEmpty()
        if (conversationId.isBlank()) {
            Timber.w("QuickReplyReceiver: missing conversationId for action=%s", intent.action)
            return
        }

        when (intent.action) {
            ACTION_REPLY -> {
                val text = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_REPLY)
                    ?.toString()
                    .orEmpty()
                    .trim()
                if (text.isEmpty()) {
                    Timber.w("QuickReplyReceiver: empty reply for conv=%s", conversationId)
                    return
                }
                handleReply(conversationId, text)
            }

            ACTION_MARK_READ -> handleMarkRead(conversationId)

            else -> Timber.w("QuickReplyReceiver unknown action: %s", intent.action)
        }
    }

    private fun handleReply(conversationId: String, text: String) {
        val pending = goAsync()
        scope.launch {
            try {
                // Optimistic insert + background send. Returns the client id immediately.
                sendMessage(conversationId, text)
                // Schedule a network-constrained retry pass so the reply is not lost
                // if this broadcast's process is killed before the send completes.
                pendingMessageScheduler.enqueue()
                // Replying implies the thread has been seen — clear the unread badge.
                markConversationRead(conversationId)
            } catch (t: Throwable) {
                Timber.e(t, "QuickReplyReceiver reply failed conv=%s", conversationId)
                // The message is persisted as PENDING; the scheduled worker retries.
                pendingMessageScheduler.enqueue()
            } finally {
                pending.finish()
            }
        }
    }

    private fun handleMarkRead(conversationId: String) {
        val pending = goAsync()
        scope.launch {
            try {
                markConversationRead(conversationId)
            } catch (t: Throwable) {
                Timber.e(t, "QuickReplyReceiver markRead failed conv=%s", conversationId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_REPLY = "com.websbaba.nitigrow.action.REPLY"
        const val ACTION_MARK_READ = "com.websbaba.nitigrow.action.MARK_READ"
        const val EXTRA_CONVERSATION_ID = "conversationId"
        const val KEY_REPLY = "key_reply"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
