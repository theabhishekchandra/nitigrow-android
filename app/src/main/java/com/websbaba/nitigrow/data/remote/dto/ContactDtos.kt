package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContactDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("isBlocked") val isBlocked: Boolean = false
)

data class ContactListResponse(
    @SerializedName("data") val data: List<ContactDto>? = null,
    @SerializedName("nextCursor") val nextCursor: String?
)

data class CreateContactRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String?,
    @SerializedName("tags") val tags: List<String>
)

data class UpdateContactRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String?,
    @SerializedName("tags") val tags: List<String>
)

data class BulkImportRequest(
    @SerializedName("contacts") val contacts: List<CreateContactRequest>
)

data class BulkImportResponse(
    @SerializedName("imported") val imported: Int,
    @SerializedName("skipped") val skipped: Int
)
