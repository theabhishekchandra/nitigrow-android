package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.CommerceApi
import com.websbaba.nitigrow.data.remote.dto.ProductDto
import com.websbaba.nitigrow.data.remote.dto.SendProductRequest
import com.websbaba.nitigrow.domain.model.Product
import com.websbaba.nitigrow.domain.repository.CommerceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommerceRepositoryImpl @Inject constructor(
    private val api: CommerceApi,
    private val dispatchers: DispatcherProvider,
) : CommerceRepository {

    private fun ProductDto.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        price = price,
        currency = currency,
        images = images.orEmpty(),
        stock = stock,
        status = status,
        syncStatus = syncStatus,
        metaProductId = metaProductId,
    )

    override suspend fun getProducts(): ApiResult<List<Product>> =
        safeApiCall(dispatchers.io) { api.listProducts() }.andThen { r ->
            ApiResult.Success(r.orEmpty().map { it.toDomain() })
        }

    override suspend fun getCatalogId(): String? =
        (safeApiCall(dispatchers.io) { api.listCatalogs() } as? ApiResult.Success)
            ?.data?.data?.firstOrNull()?.id

    override suspend fun sendProduct(
        to: String, catalogId: String, productRetailerId: String, body: String?,
    ): ApiResult<Unit> =
        safeApiCall(dispatchers.io) {
            api.sendProduct(SendProductRequest(to, catalogId, productRetailerId, body))
        }.andThen { r ->
            ApiResult.Success(Unit)
        }
}
