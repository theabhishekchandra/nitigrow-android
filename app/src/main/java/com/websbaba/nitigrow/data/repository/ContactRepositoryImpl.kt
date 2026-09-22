package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
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
        safeApiCall(dispatchers.io) { api.list() }.andThen { res ->
            dao.upsertAll((res.data ?: emptyList()).map { it.toEntity() })
            ApiResult.Success(Unit)
        }

    override suspend fun create(
        name: String,
        phone: String,
        email: String?,
        tags: List<String>
    ): ApiResult<Contact> =
        safeApiCall(dispatchers.io) {
            api.create(CreateContactRequest(name, phone, email, tags))
        }.andThen { res ->
            val entity = res.toEntity()
            dao.upsert(entity)
            ApiResult.Success(entity.toDomain())
        }

    override suspend fun update(
        id: String,
        name: String,
        phone: String,
        email: String?,
        tags: List<String>
    ): ApiResult<Contact> =
        safeApiCall(dispatchers.io) {
            api.update(id, UpdateContactRequest(name, phone, email, tags))
        }.andThen { res ->
            val entity = res.toEntity()
            dao.upsert(entity)
            ApiResult.Success(entity.toDomain())
        }

    override suspend fun delete(id: String): ApiResult<Unit> {
        dao.deleteById(id)   // optimistic
        return safeApiCall(dispatchers.io) { api.delete(id); Unit }
    }

    override suspend fun importCsv(rows: List<CsvContactRow>): ApiResult<Int> =
        safeApiCall(dispatchers.io) {
            api.bulkImport(
                BulkImportRequest(
                    rows.map { CreateContactRequest(it.name, it.phone, it.email, it.tags) }
                )
            )
        }.andThen { res ->
            refresh()
            ApiResult.Success(res.imported)
        }
}
