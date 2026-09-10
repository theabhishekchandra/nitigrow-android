package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
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
        when (val r = safeApiCall(dispatchers.io) { api.listProducts() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.orEmpty().map { it.toDomain() })
            is ApiResult.Error -> r
        }

    override suspend fun getCatalogId(): String? =
        (safeApiCall(dispatchers.io) { api.listCatalogs() } as? ApiResult.Success)
            ?.data?.data?.firstOrNull()?.id

    override suspend fun sendProduct(
        to: String, catalogId: String, productRetailerId: String, body: String?,
    ): ApiResult<Unit> =
        when (val r = safeApiCall(dispatchers.io) {
            api.sendProduct(SendProductRequest(to, catalogId, productRetailerId, body))
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }
}
