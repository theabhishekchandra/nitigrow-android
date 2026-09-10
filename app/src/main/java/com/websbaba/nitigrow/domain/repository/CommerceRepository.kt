package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Product

interface CommerceRepository {
    suspend fun getProducts(): ApiResult<List<Product>>
    /** The tenant's first Meta catalog id, or null if none is connected. */
    suspend fun getCatalogId(): String?
    suspend fun sendProduct(to: String, catalogId: String, productRetailerId: String, body: String?): ApiResult<Unit>
}
