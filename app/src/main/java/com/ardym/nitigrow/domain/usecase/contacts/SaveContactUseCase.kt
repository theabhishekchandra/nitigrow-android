package com.ardym.nitigrow.domain.usecase.contacts

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.domain.repository.ContactRepository
import javax.inject.Inject

class SaveContactUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    suspend operator fun invoke(
        id: String?,
        name: String,
        phone: String,
        email: String?,
        tags: List<String>
    ): ApiResult<Contact> {
        val cleanedPhone = phone.filter { it.isDigit() || it == '+' }
        if (name.isBlank()) return ApiResult.Error(message = "Name required")
        if (cleanedPhone.length !in 10..15) return ApiResult.Error(message = "Invalid phone")
        return if (id == null) repo.create(name.trim(), cleanedPhone, email?.trim()?.ifBlank { null }, tags)
        else repo.update(id, name.trim(), cleanedPhone, email?.trim()?.ifBlank { null }, tags)
    }
}
