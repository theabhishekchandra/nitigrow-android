package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.remote.api.SettingsApi
import com.ardym.nitigrow.data.remote.dto.AutoReplyEntryRequest
import com.ardym.nitigrow.data.remote.dto.UpdateAutoRepliesRequest
import com.ardym.nitigrow.data.remote.dto.UpdateBusinessProfileRequest
import com.ardym.nitigrow.domain.model.AppSettings
import com.ardym.nitigrow.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val api: SettingsApi,
    private val dispatchers: DispatcherProvider
) : SettingsRepository {

    override suspend fun get(): ApiResult<AppSettings> =
        when (val r = safeApiCall(dispatchers.io) { api.get() }) {
            is ApiResult.Success -> {
                val d = r.data
                ApiResult.Success(
                    AppSettings(
                        businessName = d.businessName,
                        email = d.email,
                        phone = d.phone,
                        displayPhoneNumber = d.whatsapp.displayPhoneNumber,
                        whatsappConnected = d.whatsapp.connected,
                        qualityRating = d.whatsapp.qualityRating,
                        messagingTier = d.whatsapp.messagingTier,
                        dailyLimit = d.whatsapp.dailyLimit,
                        dailyMsgCount = d.whatsapp.dailyMsgCount,
                        welcomeEnabled = d.settings.autoReplies.welcome.enabled,
                        welcomeMessage = d.settings.autoReplies.welcome.message,
                        awayEnabled = d.settings.autoReplies.away.enabled,
                        awayMessage = d.settings.autoReplies.away.message
                    )
                )
            }
            is ApiResult.Error -> r
        }

    override suspend fun updateProfile(businessName: String): ApiResult<Unit> =
        when (val r = safeApiCall(dispatchers.io) {
            api.updateProfile(UpdateBusinessProfileRequest(businessName = businessName)); Unit
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }

    override suspend fun updateAutoReplies(
        welcomeEnabled: Boolean,
        welcomeMessage: String,
        awayEnabled: Boolean,
        awayMessage: String
    ): ApiResult<Unit> =
        when (val r = safeApiCall(dispatchers.io) {
            api.updateAutoReplies(
                UpdateAutoRepliesRequest(
                    welcome = AutoReplyEntryRequest(welcomeEnabled, welcomeMessage),
                    away = AutoReplyEntryRequest(awayEnabled, awayMessage)
                )
            ); Unit
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }
}
