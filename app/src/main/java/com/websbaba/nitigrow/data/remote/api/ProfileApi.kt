package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.AvatarUploadResponse
import com.websbaba.nitigrow.data.remote.dto.DeleteAccountRequest
import com.websbaba.nitigrow.data.remote.dto.GenericMessageDto
import com.websbaba.nitigrow.data.remote.dto.InviteMemberRequest
import com.websbaba.nitigrow.data.remote.dto.InviteMemberResponse
import com.websbaba.nitigrow.data.remote.dto.MeResponseDto
import com.websbaba.nitigrow.data.remote.dto.NotificationPrefsRequest
import com.websbaba.nitigrow.data.remote.dto.TeamMemberDto
import com.websbaba.nitigrow.data.remote.dto.UpdateProfileRequest
import com.websbaba.nitigrow.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {

    @GET("me")
    suspend fun me(): UserDto

    @PATCH("me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): AvatarUploadResponse

    @GET("auth/me")
    suspend fun tenant(): MeResponseDto

    @GET("team")
    suspend fun team(): List<TeamMemberDto>

    @POST("team/invite")
    suspend fun invite(@Body body: InviteMemberRequest): InviteMemberResponse

    @DELETE("team/{id}")
    suspend fun removeMember(@Path("id") id: String): GenericMessageDto

    @PATCH("me/notifications")
    suspend fun updateNotifications(@Body body: NotificationPrefsRequest): GenericMessageDto

    @POST("me/export")
    suspend fun requestExport(): GenericMessageDto

    // Owner-only, password-confirmed. Soft-delete: backend marks the tenant
    // cancelled and erases all data within 30 days (DELETE /api/account).
    @HTTP(method = "DELETE", path = "account", hasBody = true)
    suspend fun requestAccountDelete(@Body body: DeleteAccountRequest): GenericMessageDto
}
