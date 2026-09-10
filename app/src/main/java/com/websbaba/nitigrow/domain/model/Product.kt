package com.websbaba.nitigrow.domain.model

/** A catalog product (mirrors the web/backend Commerce Product). Price is in ₹. */
data class Product(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val currency: String,
    val images: List<String>,
    val stock: Int,
    val status: String,
    val syncStatus: String,
    val metaProductId: String?,
) {
    /** Only products synced to the Meta catalog can be sent to a customer. */
    val isSynced: Boolean get() = syncStatus == "synced" && !metaProductId.isNullOrBlank()
}
