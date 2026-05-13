package com.ardym.nitigrow.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import timber.log.Timber

/**
 * Receives the inline notification actions wired by [NotificationActionsHelper].
 *
 * Currently this only logs what arrived — the parent will wire the actual
 * "send message" / "mark read" repository calls. The component is declared
 * here so the helper's `PendingIntent` targets compile, but integration with
 * [NotificationPresenter] / `NitiGrowMessagingService` is deferred.
 */
class QuickReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID).orEmpty()
        when (intent.action) {
            ACTION_REPLY -> {
                val text = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_REPLY)
                    ?.toString()
                    .orEmpty()
                Timber.d("QuickReplyReceiver ACTION_REPLY conv=%s text=%s", conversationId, text)
                // TODO: Forward to a SendMessageWorker once integration lands so the
                //  reply survives process death and is retried on transient failure.
            }

            ACTION_MARK_READ -> {
                Timber.d("QuickReplyReceiver ACTION_MARK_READ conv=%s", conversationId)
                // TODO: Forward to ConversationRepository.markRead(conversationId)
                //  and dismiss the source notification once the integration lands.
            }

            else -> Timber.w("QuickReplyReceiver unknown action: %s", intent.action)
        }
    }

    companion object {
        const val ACTION_REPLY = "com.ardym.nitigrow.action.REPLY"
        const val ACTION_MARK_READ = "com.ardym.nitigrow.action.MARK_READ"
        const val EXTRA_CONVERSATION_ID = "conversationId"
        const val KEY_REPLY = "key_reply"
    }
}
