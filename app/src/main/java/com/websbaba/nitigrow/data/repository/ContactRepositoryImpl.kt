package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.ContactDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.ContactsApi
import com.websbaba.nitigrow.data.remote.dto.BulkImportRequest
import com.websbaba.nitigrow.data.remote.dto.CreateContactRequest
import com.websbaba.nitigrow.data.remote.dto.UpdateContactRequest
import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.domain.repository.ContactRepository
import com.websbaba.nitigrow.domain.repository.CsvContactRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val api: ContactsApi,
    private val dao: ContactDao,
    private val dispatchers: DispatcherProvider
) : ContactRepository {

    override fun observeContacts(query: String): Flow<List<Contact>> {
        val source = if (query.isBlank()) dao.observeAll() else dao.search(query.lowercase())
        return source.map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun refresh(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> {
                dao.upsertAll((res.data.data ?: emptyList()).map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun create(
        name: String,
        phone: String,
        email: String?,
        tags: List<String>
    ): ApiResult<Contact> =
        when (val res = safeApiCall(dispatchers.io) {
            api.create(CreateContactRequest(name, phone, email, tags))
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toEntity()
                dao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Error -> res
        }

    override suspend fun update(
        id: String,
        name: String,
        phone: String,
        email: String?,
        tags: List<String>
    ): ApiResult<Contact> =
        when (val res = safeApiCall(dispatchers.io) {
            api.update(id, UpdateContactRequest(name, phone, email, tags))
        }) {
            is ApiResult.Success -> {
                val entity = res.data.toEntity()
                dao.upsert(entity)
                ApiResult.Success(entity.toDomain())
            }
            is ApiResult.Error -> res
        }

    override suspend fun delete(id: String): ApiResult<Unit> {
        dao.deleteById(id)   // optimistic
        return safeApiCall(dispatchers.io) { api.delete(id); Unit }
    }

    override suspend fun importCsv(rows: List<CsvContactRow>): ApiResult<Int> =
        when (val res = safeApiCall(dispatchers.io) {
            api.bulkImport(
                BulkImportRequest(
                    rows.map { CreateContactRequest(it.name, it.phone, it.email, it.tags) }
                )
            )
        }) {
            is ApiResult.Success -> {
                refresh()
                ApiResult.Success(res.data.imported)
            }
            is ApiResult.Error -> res
        }
}
