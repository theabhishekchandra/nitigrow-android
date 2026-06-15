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

// Backend returns { contacts: [...], total, page, pages } (page-based).
// `nextCursor` is absent → null → single-page sync of up to `limit` (200) rows.
data class ContactListResponse(
    @SerializedName("contacts") val data: List<ContactDto>? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null
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
