package com.ardym.nitigrow.core.notifications

import com.ardym.nitigrow.domain.repository.PushTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FCM entry point. Backend should send DATA messages (not notification messages)
 * so this service always runs and we control display + suppression.
 *
 * Expected `data` keys:
 *   type: chat | campaign | system
 *   conversationId / campaignId
 *   title, body
 */
@AndroidEntryPoint
class NitiGrowMessagingService : FirebaseMessagingService() {

    @Inject lateinit var presenter: NotificationPresenter
    @Inject lateinit var pushRepo: PushTokenRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Timber.d("FCM token rotated")
        scope.launch { pushRepo.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            "chat" -> {
                val convId = data["conversationId"] ?: return
                val name = data["title"] ?: "New message"
                val body = data["body"] ?: ""
                presenter.showChat(convId, name, body)
            }
            "campaign" -> {
                val campId = data["campaignId"] ?: return
                presenter.showCampaign(
                    campaignId = campId,
                    title = data["title"] ?: "Campaign update",
                    body = data["body"] ?: ""
                )
            }
            "system" -> {
                presenter.showSystem(
                    id = (data["id"]?.toIntOrNull()) ?: System.currentTimeMillis().toInt(),
                    title = data["title"] ?: "NitiGrow",
                    body = data["body"] ?: ""
                )
            }
            else -> Timber.w("Unknown FCM type: ${data["type"]}")
        }
    }
}
