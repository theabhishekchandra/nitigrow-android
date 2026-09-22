package com.websbaba.nitigrow.data.mapper

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.websbaba.nitigrow.data.local.entity.ConversationEntity
import com.websbaba.nitigrow.data.remote.dto.ConversationDto
import org.junit.Test
import java.time.Instant

class ConversationMapperTest {

    private val gson = Gson()

    /** A contact with no messages, exactly as the server returns it. */
    private val noMessages = """
        {"contactId":{"_id":"c1","name":"Pooja Rao","phone":"917261704270","updatedAt":"2026-09-21T17:21:46.577Z",
          "windowExpiresAt":"2026-09-22T01:21:46.557Z","isPinned":false,"isMuted":false},
         "lastMessage":null,"unreadCount":0}
    """.trimIndent()

    private val noWindow = """
        {"contactId":{"_id":"c2","name":"Manish Joshi","phone":"917984040457","updatedAt":"2026-09-21T17:21:46.576Z",
          "windowExpiresAt":null},"lastMessage":null,"unreadCount":0}
    """.trimIndent()

    private val withMessage = """
        {"contactId":{"_id":"c3","name":"Priya Reddy","phone":"919454890018","windowExpiresAt":"2026-09-22T15:00:00.000Z",
          "isPinned":true,"isMuted":true},
         "lastMessage":{"_id":"m1","text":"Works for me","createdAt":"2026-09-21T15:53:00.000Z","status":"read","direction":"inbound"},
         "unreadCount":2}
    """.trimIndent()

    @Test
    fun `a chat with no messages does not borrow the contact's updatedAt as a message time`() {
        val entity = gson.fromJson(noMessages, ConversationDto::class.java).toEntity()

        assertThat(entity.lastMessageAtEpochMs).isEqualTo(0L)
        val domain = entity.toDomain()
        assertThat(domain.hasMessages).isFalse()
        assertThat(domain.lastMessageAt).isEqualTo(Instant.EPOCH)
        assertThat(domain.lastMessage).isEmpty()
    }

    @Test
    fun `the server's window expiry is carried through`() {
        val domain = gson.fromJson(noMessages, ConversationDto::class.java).toEntity().toDomain()

        assertThat(domain.windowExpiresAt).isEqualTo(Instant.parse("2026-09-22T01:21:46.557Z"))
    }

    @Test
    fun `no window from the server means closed, not unknown`() {
        val entity = gson.fromJson(noWindow, ConversationDto::class.java).toEntity()

        assertThat(entity.windowExpiresAtEpochMs).isEqualTo(0L)
        assertThat(entity.toDomain().windowExpiresAt).isEqualTo(Instant.EPOCH)
    }

    @Test
    fun `rows cached before the column existed read as unknown`() {
        val old = ConversationEntity(
            id = "c", contactId = "c", contactName = "n", contactPhone = "p", avatarUrl = null,
            lastMessage = "", lastMessageAtEpochMs = 0, lastMessageStatus = "SENT",
            lastMessageOutbound = false, unreadCount = 0, isPinned = false, isMuted = false
        )
        assertThat(old.windowExpiresAtEpochMs).isEqualTo(ConversationEntity.WINDOW_UNKNOWN)
        assertThat(old.toDomain().windowExpiresAt).isNull()
    }

    @Test
    fun `pinned and muted come from the contact instead of being hardcoded off`() {
        val domain = gson.fromJson(withMessage, ConversationDto::class.java).toEntity().toDomain()

        assertThat(domain.isPinned).isTrue()
        assertThat(domain.isMuted).isTrue()
        assertThat(domain.unreadCount).isEqualTo(2)
        assertThat(domain.hasMessages).isTrue()
    }
}
