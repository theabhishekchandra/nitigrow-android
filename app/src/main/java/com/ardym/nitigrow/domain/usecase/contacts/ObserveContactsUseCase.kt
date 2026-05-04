package com.ardym.nitigrow.domain.usecase.contacts

import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveContactsUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    operator fun invoke(query: String): Flow<List<Contact>> =
        repo.observeContacts(query.trim())
}
