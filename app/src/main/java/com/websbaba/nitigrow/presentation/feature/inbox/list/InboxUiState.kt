package com.websbaba.nitigrow.presentation.feature.inbox.list

import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.core.util.isToday
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** Client-side quick filters shown as chips under the Inbox header. */
enum class InboxFilter { ALL, UNREAD, EXPIRING, PINNED }

data class InboxUiState(
    val query: String = "",
    val items: List<Conversation> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filter: InboxFilter = InboxFilter.ALL,
    val isFilterSheetVisible: Boolean = false,
    // Filter-sheet selections. The Conversation model carries no assignee or
    // label fields yet, so these are presentation-only: the sheet remembers
    // the choice and the list wiring lights up once the domain data exists.
    val assigneeFilter: String = DEFAULT_ASSIGNEE,
    val labelFilter: String = DEFAULT_LABEL,
) {
    val isInitialLoading: Boolean get() = items.isEmpty() && isRefreshing && error == null
    val isEmpty: Boolean get() = items.isEmpty() && !isRefreshing

    /** Conversations with at least one unread message (chip count). */
    val unreadCount: Int get() = items.count { it.unreadCount > 0 }

    /** Conversations pinned to the top of the list. */
    val pinnedCount: Int get() = items.count { it.isPinned }

    /** Conversations whose latest message landed today (subtitle under the title). */
    fun todayCount(today: LocalDate = LocalDate.now()): Int =
        items.count { it.lastMessageAt.isToday(today) }

    /** Conversations whose 24h service window closes soon (chip count). */
    fun expiringCount(now: Instant = Instant.now()): Int =
        items.count { it.windowExpiryLabel(now) != null }

    /** Items after applying the selected quick filter (client-side). */
    fun visibleItems(now: Instant = Instant.now()): List<Conversation> = when (filter) {
        InboxFilter.ALL -> items
        InboxFilter.UNREAD -> items.filter { it.unreadCount > 0 }
        InboxFilter.EXPIRING -> items.filter { it.windowExpiryLabel(now) != null }
        InboxFilter.PINNED -> items.filter { it.isPinned }
    }

    companion object {
        const val DEFAULT_ASSIGNEE = "Anyone"
        const val DEFAULT_LABEL = "All"
    }
}

// WhatsApp's customer-service window: 24h after the customer's last inbound
// message. The list model only knows the *last* message, so the window is
// derivable only while that last message is inbound; an outbound last message
// leaves the true window unknown and we report nothing rather than guess.
private val SERVICE_WINDOW: Duration = Duration.ofHours(24)
private val EXPIRING_SOON: Duration = Duration.ofHours(3)

/**
 * Returns a short "2h left" / "45m left" label when the conversation's 24-hour
 * messaging window closes within the next 3 hours, or null when the window is
 * not derivable, already closed, or comfortably open.
 */
fun Conversation.windowExpiryLabel(now: Instant = Instant.now()): String? {
    val expiresAt = replyWindowExpiresAt() ?: return null
    val remaining = Duration.between(now, expiresAt)
    if (remaining.isNegative || remaining.isZero || remaining > EXPIRING_SOON) return null
    val hours = remaining.toHours()
    return if (hours >= 1) "${hours}h left"
    else "${remaining.toMinutes().coerceAtLeast(1)}m left"
}

/**
 * When this chat's 24h reply window closes. The server's own figure wins (it knows about
 * business-initiated windows and closed ones); an [Instant.EPOCH] value means it reports no
 * open window. Only for rows cached before that field existed do we fall back to deriving it
 * from the last message — and never for a chat that has no messages, or whose last message
 * was ours, since neither says anything about the customer's window.
 */
fun Conversation.replyWindowExpiresAt(): Instant? = when {
    windowExpiresAt != null -> windowExpiresAt
    lastMessageOutbound || !hasMessages -> null
    else -> lastMessageAt.plus(SERVICE_WINDOW)
}
