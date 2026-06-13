package com.websbaba.nitigrow.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object NotificationChannels {
    const val CHAT = "chat"
    const val PAYMENT = "payment"
    const val CAMPAIGN = "campaign"
    const val SYSTEM = "system"

    /**
     * Idempotent — `createNotificationChannels` is a no-op for channels that
     * already exist (it only refreshes name/description). Called from the
     * Application class at startup and again lazily by [NotificationPresenter]
     * before the first notification is shown.
     */
    fun createAll(context: Context) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        mgr.createNotificationChannels(listOf(
            NotificationChannel(
                CHAT, "Chat messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming WhatsApp messages from your customers"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(
                PAYMENT, "Payments",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Payment confirmations and payment link activity" },
            NotificationChannel(
                CAMPAIGN, "Campaigns",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Broadcast progress and completion" },
            NotificationChannel(
                SYSTEM, "System",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Account, billing, and platform updates" }
        ))
    }
}
