package com.websbaba.nitigrow.presentation.feature.contacts.list

import com.websbaba.nitigrow.domain.model.Contact
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
    val lastSyncedAt: Instant? = null,
    /** Tag chip currently filtering the list; null shows everyone. */
    val tagFilter: String? = null,
    /** Lead pipeline size for the Leads board card; null until stats are cached. */
    val leadsTotal: Long? = null,
    val leadsNew: Long? = null
) {
    /** Items after the tag chip. (The text query is applied upstream, in the database.) */
    val visibleItems: List<Contact>
        get() = tagFilter?.let { tag -> items.filter { c -> c.tags.any { it.equals(tag, ignoreCase = true) } } }
            ?: items

    val grouped: Map<Char, List<Contact>>
        get() = visibleItems.groupBy { it.initial }.toSortedMap()

    val sectionLetters: List<Char>
        get() = grouped.keys.toList()

    /** Distinct tags across the loaded contacts — options for the tag picker. */
    val allTags: List<String>
        get() = items.flatMap { it.tags }.distinct().sorted()

    /**
     * Tags for the filter chips: most-used first, ties alphabetical. The active
     * filter is always kept, even if a search hides every contact carrying it.
     */
    val tagChips: List<String>
        get() {
            val byUse = items.flatMap { it.tags }
                .groupingBy { it }.eachCount()
                .entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .map { it.key }
            return (byUse + listOfNotNull(tagFilter)).distinct()
        }
}

sealed interface ContactsEffect {
    data class ShowMessage(val text: String) : ContactsEffect
    data object Dismiss : ContactsEffect
}
