package com.ardym.nitigrow.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.ardym.nitigrow.R

/**
 * Pure-function builders for the notification action buttons attached to the
 * chat notifications produced by [NotificationPresenter].
 *
 * The helper is kept as an `object` so it can be called from any surface
 * (presenter, messaging service, integration tests) without DI overhead.
 *
 * Both actions target [QuickReplyReceiver] via a [PendingIntent] tagged with
 * the originating conversation id so the receiver can route the side-effect
 * without consulting state.
 */
object NotificationActionsHelper {

    private const val REQUEST_CODE_REPLY = 1
    private const val REQUEST_CODE_MARK_READ = 2

    /** Reply-with-inline-input action — appears as a "Reply" chip in the shade. */
    fun replyAction(context: Context, conversationId: String): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(QuickReplyReceiver.KEY_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, QuickReplyReceiver::class.java).apply {
            action = QuickReplyReceiver.ACTION_REPLY
            putExtra(QuickReplyReceiver.EXTRA_CONVERSATION_ID, conversationId)
        }
        val replyPending = PendingIntent.getBroadcast(
            context,
            // Mix conversationId into the request code so per-chat PendingIntents
            // don't collide (FLAG_UPDATE_CURRENT would otherwise overwrite the
            // wrong notification's extras).
            REQUEST_CODE_REPLY + conversationId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_stat_nitigrow,
            "Reply",
            replyPending
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()
    }

    /** Fire-and-forget "Mark as read" action — no input, dismisses on tap. */
    fun markReadAction(context: Context, conversationId: String): NotificationCompat.Action {
        val intent = Intent(context, QuickReplyReceiver::class.java).apply {
            action = QuickReplyReceiver.ACTION_MARK_READ
            putExtra(QuickReplyReceiver.EXTRA_CONVERSATION_ID, conversationId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MARK_READ + conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_stat_nitigrow,
            "Mark as read",
            pending
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()
    }
}
