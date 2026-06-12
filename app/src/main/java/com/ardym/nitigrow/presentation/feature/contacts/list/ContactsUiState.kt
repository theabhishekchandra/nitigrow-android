package com.ardym.nitigrow.presentation.feature.contacts.list

import com.ardym.nitigrow.domain.model.Contact
import java.time.Instant

data class ContactsUiState(
    val query: String = "",
    val items: List<Contact> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val editing: Contact? = null,
    val sheetOpen: Boolean = false,
    val savingSheet: Boolean = false,
    val sheetError: String? = null,
    /** Set after each successful refresh; drives the "synced X ago" header line. */
    val lastSyncedAt: Instant? = null
) {
    val grouped: Map<Char, List<Contact>>
        get() = items.groupBy { it.initial }.toSortedMap()

    val sectionLetters: List<Char>
        get() = grouped.keys.toList()

    /** Distinct tags across the loaded contacts — options for the tag picker. */
    val allTags: List<String>
        get() = items.flatMap { it.tags }.distinct().sorted()
}

sealed interface ContactsEffect {
    data class ShowMessage(val text: String) : ContactsEffect
    data object Dismiss : ContactsEffect
}
