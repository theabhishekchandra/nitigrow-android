package com.websbaba.nitigrow.presentation.feature.inbox.list

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.model.MessageStatus
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class InboxUiStateTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    private fun conversation(
        id: String,
        unread: Int = 0,
        pinned: Boolean = false,
        lastMessageAt: Instant = now,
        outbound: Boolean = false,
    ) = Conversation(
        id = id,
        contactId = "c-$id",
        contactName = "Contact $id",
        contactPhone = "+9199000000$id",
        avatarUrl = null,
        lastMessage = "hello",
        lastMessageAt = lastMessageAt,
        lastMessageStatus = MessageStatus.DELIVERED,
        lastMessageOutbound = outbound,
        unreadCount = unread,
        isPinned = pinned,
        isMuted = false
    )

    private val items = listOf(
        conversation("1", unread = 2),
        conversation("2", pinned = true),
        // Inbound 22h ago → its 24h window closes in 2h → "expiring".
        conversation("3", lastMessageAt = now.minus(22, ChronoUnit.HOURS)),
        // Outbound last message → window unknown → never "expiring".
        conversation("4", lastMessageAt = now.minus(22, ChronoUnit.HOURS), outbound = true)
    )

    @Test
    fun `each quick filter selects its own subset`() {
        val base = InboxUiState(items = items)

        assertThat(base.copy(filter = InboxFilter.ALL).visibleItems(now).map { it.id })
            .containsExactly("1", "2", "3", "4").inOrder()
        assertThat(base.copy(filter = InboxFilter.UNREAD).visibleItems(now).map { it.id })
            .containsExactly("1")
        assertThat(base.copy(filter = InboxFilter.PINNED).visibleItems(now).map { it.id })
            .containsExactly("2")
        assertThat(base.copy(filter = InboxFilter.EXPIRING).visibleItems(now).map { it.id })
            .containsExactly("3")
    }

    @Test
    fun `chip counts reflect the unfiltered list`() {
        val state = InboxUiState(items = items, filter = InboxFilter.PINNED)

        assertThat(state.unreadCount).isEqualTo(1)
        assertThat(state.pinnedCount).isEqualTo(1)
        assertThat(state.expiringCount(now)).isEqualTo(1)
    }

    @Test
    fun `today count only includes conversations from the given day`() {
        val zone = java.time.ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val state = InboxUiState(
            items = listOf(
                conversation("1", lastMessageAt = now),
                conversation("2", lastMessageAt = now.minus(3, ChronoUnit.DAYS))
            )
        )

        assertThat(state.todayCount(today)).isEqualTo(1)
    }
}

class ReplyWindowTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    private fun conv(
        lastAt: Instant,
        outbound: Boolean = false,
        window: Instant? = null,
    ) = Conversation(
        id = "1", contactId = "c", contactName = "n", contactPhone = "p", avatarUrl = null,
        lastMessage = if (lastAt == Instant.EPOCH) "" else "hi", lastMessageAt = lastAt,
        lastMessageStatus = MessageStatus.DELIVERED, lastMessageOutbound = outbound,
        unreadCount = 0, isPinned = false, isMuted = false, windowExpiresAt = window
    )

    @Test
    fun `the server's expiry wins over guessing from the last message`() {
        // Last inbound 22h ago would suggest 2h left, but the server says 7h.
        val c = conv(lastAt = now.minus(22, ChronoUnit.HOURS), window = now.plus(7, ChronoUnit.HOURS))

        assertThat(c.replyWindowExpiresAt()).isEqualTo(now.plus(7, ChronoUnit.HOURS))
        assertThat(c.windowExpiryLabel(now)).isNull() // 7h is not "expiring soon"
    }

    @Test
    fun `a server expiry inside three hours is flagged as expiring`() {
        val c = conv(lastAt = now.minus(1, ChronoUnit.HOURS), outbound = true, window = now.plus(90, ChronoUnit.MINUTES))

        assertThat(c.windowExpiryLabel(now)).isEqualTo("1h left")
    }

    @Test
    fun `no window from the server is closed and never expiring`() {
        val c = conv(lastAt = now.minus(22, ChronoUnit.HOURS), window = Instant.EPOCH)

        assertThat(c.replyWindowExpiresAt()).isEqualTo(Instant.EPOCH)
        assertThat(c.windowExpiryLabel(now)).isNull()
    }

    @Test
    fun `unknown expiry is only derived from a real inbound message`() {
        val inbound = now.minus(22, ChronoUnit.HOURS)

        assertThat(conv(lastAt = inbound).replyWindowExpiresAt()).isEqualTo(inbound.plus(24, ChronoUnit.HOURS))
        assertThat(conv(lastAt = inbound, outbound = true).replyWindowExpiresAt()).isNull()
        assertThat(conv(lastAt = Instant.EPOCH).replyWindowExpiresAt()).isNull()
    }
}
