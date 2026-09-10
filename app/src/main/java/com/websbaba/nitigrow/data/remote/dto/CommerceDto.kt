package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("images") val images: List<String>? = null,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("status") val status: String = "active",
    @SerializedName("syncStatus") val syncStatus: String = "pending",
    @SerializedName("metaProductId") val metaProductId: String? = null,
)

// /catalog/list stays a { data } wrapper (plain res.json, not an envelope, so
// mobileCompat does not unwrap it). /commerce/products IS an envelope, so mobile
// gets a bare array — see CommerceApi.listProducts.
data class CatalogDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
)
data class CatalogListResponse(@SerializedName("data") val data: List<CatalogDto>? = null)

data class SendProductRequest(
    @SerializedName("to") val to: String,
    @SerializedName("catalogId") val catalogId: String,
    @SerializedName("productRetailerId") val productRetailerId: String,
    @SerializedName("body") val body: String? = null,
)

// send endpoints reply { data: <graph result> } — shape is Meta's, so keep it opaque.
data class SendResultDto(@SerializedName("data") val data: JsonElement? = null)
