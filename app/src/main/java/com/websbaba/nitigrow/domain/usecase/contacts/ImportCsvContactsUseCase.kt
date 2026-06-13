package com.websbaba.nitigrow.domain.usecase.contacts

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.ContactRepository
import com.websbaba.nitigrow.domain.repository.CsvContactRow
import javax.inject.Inject

class ImportCsvContactsUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    /**
     * Expects CSV with header: name,phone,email,tags
     * tags = pipe-separated.
     */
    suspend operator fun invoke(csvText: String): ApiResult<Int> {
        val lines = csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.size <= 1) return ApiResult.Error(message = "Empty CSV")
        val header = lines.first().split(",").map { it.trim().lowercase() }
        val nameIdx = header.indexOf("name").takeIf { it >= 0 }
            ?: return ApiResult.Error(message = "Missing 'name' column")
        val phoneIdx = header.indexOf("phone").takeIf { it >= 0 }
            ?: return ApiResult.Error(message = "Missing 'phone' column")
        val emailIdx = header.indexOf("email")
        val tagsIdx = header.indexOf("tags")

        val rows = lines.drop(1).mapNotNull { line ->
            val cols = line.split(",").map { it.trim() }
            val name = cols.getOrNull(nameIdx).orEmpty()
            val phone = cols.getOrNull(phoneIdx).orEmpty().filter { it.isDigit() || it == '+' }
            if (name.isBlank() || phone.length !in 10..15) return@mapNotNull null
            CsvContactRow(
                name = name,
                phone = phone,
                email = cols.getOrNull(emailIdx)?.takeIf { it.isNotBlank() },
                tags = cols.getOrNull(tagsIdx).orEmpty()
                    .split('|').map { it.trim() }.filter { it.isNotBlank() }
            )
        }
        if (rows.isEmpty()) return ApiResult.Error(message = "No valid rows")
        return repo.importCsv(rows)
    }
}
