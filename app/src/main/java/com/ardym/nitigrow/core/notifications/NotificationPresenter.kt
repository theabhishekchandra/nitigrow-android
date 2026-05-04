package com.ardym.nitigrow.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
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

    fun showChat(
        conversationId: String,
        contactName: String,
        body: String
    ) {
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
        val notif = NotificationCompat.Builder(context, NotificationChannels.CHAT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contactName)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        mgr?.notify(conversationId.hashCode(), notif)
    }

    fun showCampaign(campaignId: String, title: String, body: String) {
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        mgr?.notify(campaignId.hashCode(), notif)
    }

    fun showSystem(id: Int, title: String, body: String) {
        val notif = NotificationCompat.Builder(context, NotificationChannels.SYSTEM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        mgr?.notify(id, notif)
    }

    fun cancel(tag: String) {
        mgr?.cancel(tag.hashCode())
    }
}
