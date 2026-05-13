package com.ardym.nitigrow.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object NotificationChannels {
    const val CHAT = "chat"
    const val CAMPAIGN = "campaign"
    const val SYSTEM = "system"

    fun createAll(context: Context) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        mgr.createNotificationChannels(listOf(
            NotificationChannel(
                CHAT, "Chats",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Inbound WhatsApp messages"
                enableLights(true)
                enableVibration(true)
            },
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
