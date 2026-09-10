package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.CatalogListResponse
import com.websbaba.nitigrow.data.remote.dto.ProductDto
import com.websbaba.nitigrow.data.remote.dto.SendProductRequest
import com.websbaba.nitigrow.data.remote.dto.SendResultDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Commerce: catalog products + sending a product card to a customer. BASE_URL ends in /api/. */
interface CommerceApi {
    // /commerce/products uses the ResponseHandler envelope, which mobileCompat
    // unwraps to a BARE array for x-client:mobile — so this is List<ProductDto>,
    // not a { data } wrapper.
    @GET("commerce/products")
    suspend fun listProducts(): List<ProductDto>

    @GET("catalog/list")
    suspend fun listCatalogs(): CatalogListResponse

    @POST("catalog/send/product")
    suspend fun sendProduct(@Body body: SendProductRequest): SendResultDto
}
