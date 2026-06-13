package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.BulkImportRequest
import com.websbaba.nitigrow.data.remote.dto.BulkImportResponse
import com.websbaba.nitigrow.data.remote.dto.ContactDto
import com.websbaba.nitigrow.data.remote.dto.ContactListResponse
import com.websbaba.nitigrow.data.remote.dto.CreateContactRequest
import com.websbaba.nitigrow.data.remote.dto.GenericMessageDto
import com.websbaba.nitigrow.data.remote.dto.UpdateContactRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ContactsApi {

    @GET("contacts")
    suspend fun list(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 200
    ): ContactListResponse

    @POST("contacts")
    suspend fun create(@Body body: CreateContactRequest): ContactDto

    @PATCH("contacts/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdateContactRequest
    ): ContactDto

    @DELETE("contacts/{id}")
    suspend fun delete(@Path("id") id: String): GenericMessageDto

    @POST("contacts/import")
    suspend fun bulkImport(@Body body: BulkImportRequest): BulkImportResponse
}
