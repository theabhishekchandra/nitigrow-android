package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.AvatarUploadResponse
import com.ardym.nitigrow.data.remote.dto.DeleteAccountRequest
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.InviteMemberRequest
import com.ardym.nitigrow.data.remote.dto.NotificationPrefsRequest
import com.ardym.nitigrow.data.remote.dto.TeamListResponse
import com.ardym.nitigrow.data.remote.dto.TeamMemberDto
import com.ardym.nitigrow.data.remote.dto.TenantDto
import com.ardym.nitigrow.data.remote.dto.UpdateProfileRequest
import com.ardym.nitigrow.data.remote.dto.UserDto
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

    @GET("tenant")
    suspend fun tenant(): TenantDto

    @GET("team")
    suspend fun team(): TeamListResponse

    @POST("team/invite")
    suspend fun invite(@Body body: InviteMemberRequest): TeamMemberDto

    @DELETE("team/{id}")
    suspend fun removeMember(@Path("id") id: String): GenericMessageDto

    @PATCH("me/notifications")
    suspend fun updateNotifications(@Body body: NotificationPrefsRequest): GenericMessageDto

    @POST("me/export")
    suspend fun requestExport(): GenericMessageDto

    @HTTP(method = "DELETE", path = "me", hasBody = true)
    suspend fun requestAccountDelete(@Body body: DeleteAccountRequest): GenericMessageDto
}
