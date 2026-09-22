package com.websbaba.nitigrow.presentation.feature.contacts.list

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.domain.model.Contact
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.formatPhoneForDisplay
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.phoneForEditing
import com.websbaba.nitigrow.presentation.feature.contacts.list.components.phoneForSaving
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ContactsUiStateTest {

    private fun contact(id: String, name: String, vararg tags: String) = Contact(
        id = id, name = name, phone = "+9199000000$id", email = null, avatarUrl = null,
        tags = tags.toList(), notes = null, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH, isBlocked = false
    )

    private val items = listOf(
        contact("1", "Anita Desai", "VIP"),
        contact("2", "Arjun Mehta"),
        contact("3", "Kavita Joshi", "New"),
        contact("4", "Priya Sharma", "VIP", "Diwali"),
        contact("5", "Rahul Verma", "Wholesale"),
        contact("6", "Meera Iyer", "Wholesale"),
    )

    @Test
    fun `tag filter keeps only contacts carrying the tag, ignoring case`() {
        val state = ContactsUiState(items = items, tagFilter = "vip")

        assertThat(state.visibleItems.map { it.id }).containsExactly("1", "4")
        assertThat(ContactsUiState(items = items).visibleItems).hasSize(6)
    }

    @Test
    fun `sections are built from the filtered contacts in A to Z order`() {
        val all = ContactsUiState(items = items)
        assertThat(all.sectionLetters).containsExactly('A', 'K', 'M', 'P', 'R').inOrder()

        val vip = ContactsUiState(items = items, tagFilter = "VIP")
        assertThat(vip.sectionLetters).containsExactly('A', 'P').inOrder()
    }

    @Test
    fun `chips are ordered by use then alphabetically`() {
        val state = ContactsUiState(items = items)

        // VIP ×2, Wholesale ×2, then Diwali and New (×1) alphabetically.
        assertThat(state.tagChips).containsExactly("VIP", "Wholesale", "Diwali", "New").inOrder()
    }

    @Test
    fun `the active filter chip stays even when a search hides its contacts`() {
        val searched = ContactsUiState(items = listOf(contact("2", "Arjun Mehta")), tagFilter = "VIP")

        assertThat(searched.tagChips).containsExactly("VIP")
        assertThat(searched.visibleItems).isEmpty()
    }

    @Test
    fun `contacts without a letter group under hash`() {
        val state = ContactsUiState(items = listOf(contact("9", "  123 Traders")))
        assertThat(state.sectionLetters).containsExactly('T')
        assertThat(ContactsUiState(items = listOf(contact("8", "007"))).sectionLetters).containsExactly('#')
    }
}

class ContactsHelpersTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    @Test
    fun `phone shown for editing drops the country prefix`() {
        assertThat(phoneForEditing("+91 98765 43210")).isEqualTo("98765 43210")
        assertThat(phoneForEditing("+919876543210")).isEqualTo("9876543210")
        assertThat(phoneForEditing("+14155550100")).isEqualTo("+14155550100")
        // The backend also stores Indian numbers as bare "91" + 10 digits (no plus).
        assertThat(phoneForEditing("919622731324")).isEqualTo("9622731324")
        assertThat(phoneForEditing("9622731324")).isEqualTo("9622731324")
    }

    @Test
    fun `phone to save defaults to plus 91 and keeps an explicit country code`() {
        assertThat(phoneForSaving("98765 43210")).isEqualTo("+919876543210")
        assertThat(phoneForSaving("+1 415 555 0100")).isEqualTo("+14155550100")
        assertThat(phoneForSaving("98-765")).isEqualTo("+9198765")
    }

    @Test
    fun `list shows Indian mobiles grouped and leaves other numbers alone`() {
        assertThat(formatPhoneForDisplay("919622731324")).isEqualTo("+91 96227 31324")
        assertThat(formatPhoneForDisplay("+919622731324")).isEqualTo("+91 96227 31324")
        assertThat(formatPhoneForDisplay("+14155550100")).isEqualTo("+14155550100")
        assertThat(formatPhoneForDisplay("12345")).isEqualTo("12345")
    }

    @Test
    fun `sync label steps through minutes, hours and days`() {
        assertThat(agoLabel(now.minusSeconds(20), now)).isEqualTo("just now")
        assertThat(agoLabel(now.minus(2, ChronoUnit.MINUTES), now)).isEqualTo("2 min ago")
        assertThat(agoLabel(now.minus(3, ChronoUnit.HOURS), now)).isEqualTo("3 hr ago")
        assertThat(agoLabel(now.minus(2, ChronoUnit.DAYS), now)).isEqualTo("2 d ago")
    }

    @Test
    fun `CRM subtitle groups thousands and mentions sync only after one`() {
        assertThat(crmSubtitle(1240, null, now)).isEqualTo("1,240 in CRM")
        assertThat(crmSubtitle(1240, now.minus(2, ChronoUnit.MINUTES), now)).isEqualTo("1,240 in CRM · synced 2 min ago")
    }

    @Test
    fun `leads subtitle degrades gracefully`() {
        assertThat(leadsSubtitle(null, null)).isEqualTo("Your lead pipeline")
        assertThat(leadsSubtitle(148, 12)).isEqualTo("148 leads · 12 new")
        assertThat(leadsSubtitle(148, 0)).isEqualTo("148 leads")
        assertThat(leadsSubtitle(1, null)).isEqualTo("1 lead")
    }
}
