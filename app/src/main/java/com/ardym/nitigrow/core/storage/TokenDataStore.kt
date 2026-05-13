package com.ardym.nitigrow.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ardym.nitigrow.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.DATASTORE_NAME)

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAccess = stringPreferencesKey("access_token")
    private val keyRefresh = stringPreferencesKey("refresh_token")
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyTenantId = stringPreferencesKey("tenant_id")
    private val keyOnboardingDone = booleanPreferencesKey("onboarding_done")
    private val keyBiometricEnabled = booleanPreferencesKey("biometric_enabled")
    private val keyChatNotif = booleanPreferencesKey("chat_notif")
    private val keyCampaignNotif = booleanPreferencesKey("campaign_notif")
    private val keySystemNotif = booleanPreferencesKey("system_notif")
    private val keySoundNotif = booleanPreferencesKey("sound_notif")
    private val keyPreviewVisible = booleanPreferencesKey("preview_visible")
    private val keyLanguageTag = stringPreferencesKey("language_tag")

    val accessToken: Flow<String?> = context.dataStore.data.map { it[keyAccess] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[keyRefresh] }
    val userId: Flow<String?> = context.dataStore.data.map { it[keyUserId] }
    val tenantId: Flow<String?> = context.dataStore.data.map { it[keyTenantId] }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[keyOnboardingDone] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyBiometricEnabled] ?: false }
    val chatNotifEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyChatNotif] ?: true }
    val campaignNotifEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyCampaignNotif] ?: true }
    val systemNotifEnabled: Flow<Boolean> = context.dataStore.data.map { it[keySystemNotif] ?: true }
    val notifSoundEnabled: Flow<Boolean> = context.dataStore.data.map { it[keySoundNotif] ?: true }
    val notifPreviewVisible: Flow<Boolean> = context.dataStore.data.map { it[keyPreviewVisible] ?: true }
    val languageTag: Flow<String> = context.dataStore.data.map { it[keyLanguageTag] ?: "" }

    suspend fun accessTokenBlocking(): String? = accessToken.first()

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[keyOnboardingDone] = true }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyBiometricEnabled] = enabled }
    }

    suspend fun setNotificationPrefs(
        chat: Boolean, campaign: Boolean, system: Boolean,
        sound: Boolean, preview: Boolean
    ) {
        context.dataStore.edit {
            it[keyChatNotif] = chat
            it[keyCampaignNotif] = campaign
            it[keySystemNotif] = system
            it[keySoundNotif] = sound
            it[keyPreviewVisible] = preview
        }
    }

    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { it[keyLanguageTag] = tag }
    }

    suspend fun saveSession(access: String, refresh: String, userId: String, tenantId: String) {
        context.dataStore.edit {
            it[keyAccess] = access
            it[keyRefresh] = refresh
            it[keyUserId] = userId
            it[keyTenantId] = tenantId
        }
    }

    suspend fun updateAccessToken(access: String) {
        context.dataStore.edit { it[keyAccess] = access }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
