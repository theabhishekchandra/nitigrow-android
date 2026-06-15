package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.NotificationPreferences
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.model.Tenant
import com.websbaba.nitigrow.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<User?>
    fun observeTenant(): Flow<Tenant?>
    fun observeTeam(): Flow<List<TeamMember>>
    fun observeNotificationPreferences(): Flow<NotificationPreferences>
    fun observeLanguage(): Flow<String>

    suspend fun refreshProfile(): ApiResult<Unit>
    suspend fun refreshTenant(): ApiResult<Unit>
    suspend fun refreshTeam(): ApiResult<Unit>

    suspend fun updateProfile(name: String, email: String): ApiResult<User>
    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): ApiResult<String>

    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): ApiResult<Unit>
    suspend fun setLanguage(tag: String)

    suspend fun requestDataExport(): ApiResult<Unit>
    suspend fun requestAccountDelete(password: String): ApiResult<Unit>

    suspend fun inviteMember(email: String, role: String): ApiResult<TeamMember>
    suspend fun removeMember(memberId: String): ApiResult<Unit>
}
