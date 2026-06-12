package com.ardym.nitigrow.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.ardym.nitigrow.MainActivity
import com.ardym.nitigrow.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foregroundChat: ForegroundChatTracker
) {
    private val mgr by lazy { context.getSystemService<NotificationManager>() }

    /**
     * Channels are also created in the Application class; this lazy guard keeps
     * the presenter safe if a push arrives before that hook has run (e.g. a
     * direct-boot edge case). [NotificationChannels.createAll] is idempotent.
     */
    private val channelsReady: Boolean by lazy {
        NotificationChannels.createAll(context)
        true
    }

    private val brandColor: Int
        get() = ContextCompat.getColor(context, R.color.brand)

    fun showChat(
        conversationId: String,
        contactName: String,
        body: String
    ) {
        check(channelsReady)
        if (foregroundChat.isOpen(conversationId)) return
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("nitigrow://chat/$conversationId"),
            context, MainActivity::class.java
        ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi = PendingIntent.getActivity(
            context, conversationId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sender = Person.Builder()
            .setName(contactName)
            .setKey(conversationId)
            .setIcon(IconCompat.createWithBitmap(avatarBitmap(contactName)))
            .build()

        // Rebuild the conversation transcript from the currently posted
        // notification (if any) so consecutive pushes stack inside one
        // MessagingStyle card instead of overwriting each other.
        val style = restoreMessagingStyle(conversationId.hashCode())
            ?: NotificationCompat.MessagingStyle(ME)
        style.addMessage(body, System.currentTimeMillis(), sender)

        val notif = NotificationCompat.Builder(context, NotificationChannels.CHAT)
            .setSmallIcon(R.drawable.ic_stat_nitigrow)
            .setStyle(style)
            .setColor(brandColor)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(conversationId)
            .setGroup(GROUP_CHATS)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .addAction(NotificationActionsHelper.replyAction(context, conversationId))
            .addAction(NotificationActionsHelper.markReadAction(context, conversationId))
            .build()
        mgr?.notify(conversationId.hashCode(), notif)
        postChatGroupSummary()
    }

    fun showPayment(paymentId: String, title: String, body: String) {
        check(channelsReady)
        val intent = Intent(context, MainActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi = PendingIntent.getActivity(
            context, paymentId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Design: title carries the key fact ("₹2,450 received from Kavita Reddy"),
        // body carries the context ("Payment link · gift hampers").
        val notif = NotificationCompat.Builder(context, NotificationChannels.PAYMENT)
            .setSmallIcon(R.drawable.ic_stat_nitigrow)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(brandColor)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        mgr?.notify(paymentId.hashCode(), notif)
    }

    fun showCampaign(campaignId: String, title: String, body: String) {
        check(channelsReady)
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("nitigrow://campaign/$campaignId"),
            context, MainActivity::class.java
        ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi = PendingIntent.getActivity(
            context, campaignId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, NotificationChannels.CAMPAIGN)
            .setSmallIcon(R.drawable.ic_stat_nitigrow)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(brandColor)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        mgr?.notify(campaignId.hashCode(), notif)
    }

    fun showSystem(id: Int, title: String, body: String) {
        check(channelsReady)
        val notif = NotificationCompat.Builder(context, NotificationChannels.SYSTEM)
            .setSmallIcon(R.drawable.ic_stat_nitigrow)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(brandColor)
            .setAutoCancel(true)
            .build()
        mgr?.notify(id, notif)
    }

    fun cancel(tag: String) {
        mgr?.cancel(tag.hashCode())
    }

    /** Pulls the MessagingStyle out of an already-posted chat notification. */
    private fun restoreMessagingStyle(notificationId: Int): NotificationCompat.MessagingStyle? {
        val existing = mgr?.activeNotifications
            ?.firstOrNull { it.id == notificationId }
            ?.notification
            ?: return null
        return NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(existing)
    }

    /**
     * One summary card bundles every per-conversation chat notification in the
     * shade (children alert; the summary itself stays silent).
     */
    private fun postChatGroupSummary() {
        val intent = Intent(context, MainActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pi = PendingIntent.getActivity(
            context, GROUP_SUMMARY_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val summary = NotificationCompat.Builder(context, NotificationChannels.CHAT)
            .setSmallIcon(R.drawable.ic_stat_nitigrow)
            .setContentTitle("New messages")
            .setColor(brandColor)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setGroup(GROUP_CHATS)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        mgr?.notify(GROUP_SUMMARY_ID, summary)
    }

    /**
     * Initials drawn on a circle, colored from the design's 7-pair warm avatar
     * palette, picked by a stable hash of the sender name — mirrors the in-app
     * Avatar component so the shade matches the inbox.
     */
    private fun avatarBitmap(name: String): Bitmap {
        val (bg, fg) = AVATAR_PALETTE[stableHash(name) % AVATAR_PALETTE.size]
        val size = AVATAR_SIZE_PX
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val half = size / 2f

        canvas.drawCircle(half, half, half, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg })

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fg
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = size * 0.38f
            textAlign = Paint.Align.CENTER
        }
        val baselineY = half - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initials(name), half, baselineY, textPaint)
        return bitmap
    }

    private fun initials(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.isEmpty() -> "?"
            words.size == 1 -> words[0].take(1).uppercase()
            else -> (words.first().take(1) + words.last().take(1)).uppercase()
        }
    }

    private fun stableHash(name: String): Int {
        var hash = 0
        for (c in name) hash = (hash * 31 + c.code) and 0x7FFFFFFF
        return hash
    }

    private companion object {
        val ME: Person = Person.Builder().setName("You").setKey("me").build()

        const val GROUP_CHATS = "com.ardym.nitigrow.group.CHATS"
        const val GROUP_SUMMARY_ID = -0x47A75 // stable, out of conversationId.hashCode() hot range

        const val AVATAR_SIZE_PX = 128

        /** Design's warm avatar palette — bg to fg (initials) pairs. */
        val AVATAR_PALETTE: List<Pair<Int, Int>> = listOf(
            0xFFF5D78C.toInt() to 0xFF6B4A0F.toInt(),
            0xFFF5B7A0.toInt() to 0xFF7A2F1A.toInt(),
            0xFFC8D9B0.toInt() to 0xFF3A5223.toInt(),
            0xFFE8A94A.toInt() to 0xFF4A2F0A.toInt(),
            0xFFB5D4D0.toInt() to 0xFF1F4845.toInt(),
            0xFFD9C4E8.toInt() to 0xFF422D5A.toInt(),
            0xFFF0C9B5.toInt() to 0xFF5A2A12.toInt()
        )
    }
}
