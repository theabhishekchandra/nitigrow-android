package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.ProfileDao
import com.websbaba.nitigrow.data.local.dao.TeamDao
import com.websbaba.nitigrow.data.local.dao.TenantDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toDomainUser
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.mapper.toProfileEntity
import com.websbaba.nitigrow.data.remote.api.ProfileApi
import com.websbaba.nitigrow.data.remote.dto.DeleteAccountRequest
import com.websbaba.nitigrow.data.remote.dto.InviteMemberRequest
import com.websbaba.nitigrow.data.remote.dto.NotificationPrefsRequest
import com.websbaba.nitigrow.data.remote.dto.UpdateProfileRequest
import com.websbaba.nitigrow.domain.model.NotificationPreferences
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.model.Tenant
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val profileDao: ProfileDao,
    private val tenantDao: TenantDao,
    private val teamDao: TeamDao,
    private val tokenStore: TokenDataStore,
    private val dispatchers: DispatcherProvider
) : ProfileRepository {

    override fun observeProfile(): Flow<User?> =
        profileDao.observe().map { it?.toDomainUser() }

    override fun observeTenant(): Flow<Tenant?> =
        tenantDao.observe().map { it?.toDomain() }

    override fun observeTeam(): Flow<List<TeamMember>> =
        teamDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeNotificationPreferences(): Flow<NotificationPreferences> =
        combine(
            tokenStore.chatNotifEnabled,
            tokenStore.campaignNotifEnabled,
            tokenStore.systemNotifEnabled,
            tokenStore.notifSoundEnabled,
            tokenStore.notifPreviewVisible
        ) { chat, campaign, system, sound, preview ->
            NotificationPreferences(chat, campaign, system, sound, preview)
        }

    override fun observeLanguage(): Flow<String> = tokenStore.languageTag

    override suspend fun refreshProfile(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.me() }) {
            is ApiResult.Success -> {
                profileDao.upsert(res.data.toProfileEntity())
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun refreshTenant(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.tenant() }) {
            is ApiResult.Success -> {
                res.data.tenant?.let { tenantDao.upsert(it.toEntity()) }
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun refreshTeam(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.team() }) {
            is ApiResult.Success -> {
                teamDao.upsertAll((res.data.data ?: emptyList()).map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun updateProfile(name: String, email: String): ApiResult<User> =
        when (val res = safeApiCall(dispatchers.io) {
            api.updateProfile(UpdateProfileRequest(name, email))
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toProfileEntity()
                profileDao.upsert(entity)
                ApiResult.Success(entity.toDomainUser())
            }
            is ApiResult.Error -> res
        }

    override suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): ApiResult<String> {
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val ext = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val part = MultipartBody.Part.createFormData("file", "avatar.$ext", body)
        return safeApiCall(dispatchers.io) { api.uploadAvatar(part).avatarUrl }
    }

    override suspend fun updateNotificationPreferences(
        prefs: NotificationPreferences
    ): ApiResult<Unit> {
        // local first; backend is best-effort sync
        tokenStore.setNotificationPrefs(
            chat = prefs.chatEnabled,
            campaign = prefs.campaignEnabled,
            system = prefs.systemEnabled,
            sound = prefs.soundEnabled,
            preview = prefs.previewVisible
        )
        return safeApiCall(dispatchers.io) {
            api.updateNotifications(
                NotificationPrefsRequest(
                    chat = prefs.chatEnabled,
                    campaign = prefs.campaignEnabled,
                    system = prefs.systemEnabled,
                    sound = prefs.soundEnabled,
                    preview = prefs.previewVisible
                )
            ); Unit
        }
    }

    override suspend fun setLanguage(tag: String) {
        tokenStore.setLanguageTag(tag)
    }

    override suspend fun requestDataExport(): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.requestExport(); Unit }

    override suspend fun requestAccountDelete(password: String): ApiResult<Unit> =
        safeApiCall(dispatchers.io) {
            api.requestAccountDelete(DeleteAccountRequest(password)); Unit
        }

    override suspend fun inviteMember(email: String, role: String): ApiResult<TeamMember> =
        when (val res = safeApiCall(dispatchers.io) {
            api.invite(InviteMemberRequest(email, role))
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toEntity()
                teamDao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Error -> res
        }

    override suspend fun removeMember(memberId: String): ApiResult<Unit> {
        teamDao.deleteById(memberId)
        return safeApiCall(dispatchers.io) { api.removeMember(memberId); Unit }
    }
}
