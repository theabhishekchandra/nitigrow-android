package com.ardym.nitigrow.presentation.feature.contacts.list

import com.ardym.nitigrow.domain.model.Contact

data class ContactsUiState(
    val query: String = "",
    val items: List<Contact> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val editing: Contact? = null,
    val sheetOpen: Boolean = false,
    val savingSheet: Boolean = false,
    val sheetError: String? = null
) {
    val grouped: Map<Char, List<Contact>>
        get() = items.groupBy { it.initial }.toSortedMap()

    val sectionLetters: List<Char>
        get() = grouped.keys.toList()
}

sealed interface ContactsEffect {
    data class ShowMessage(val text: String) : ContactsEffect
    data object Dismiss : ContactsEffect
}
