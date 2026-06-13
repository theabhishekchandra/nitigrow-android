package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun observeContacts(query: String = ""): Flow<List<Contact>>
    suspend fun refresh(): ApiResult<Unit>
    suspend fun create(name: String, phone: String, email: String?, tags: List<String>): ApiResult<Contact>
    suspend fun update(id: String, name: String, phone: String, email: String?, tags: List<String>): ApiResult<Contact>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun importCsv(rows: List<CsvContactRow>): ApiResult<Int>
}

data class CsvContactRow(
    val name: String,
    val phone: String,
    val email: String? = null,
    val tags: List<String> = emptyList()
)
