package com.websbaba.nitigrow.domain.usecase.contacts

import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveContactsUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    operator fun invoke(query: String): Flow<List<Contact>> =
        repo.observeContacts(query.trim())
}
