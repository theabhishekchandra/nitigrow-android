package com.websbaba.nitigrow.presentation.feature.dashboard

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import com.websbaba.nitigrow.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.websbaba.nitigrow.domain.usecase.dashboard.RefreshDashboardUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.RefreshInboxUseCase
import com.websbaba.nitigrow.domain.usecase.profile.ObserveProfileUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val getStats: GetDashboardStatsUseCase = mockk()
    private val observeProfile: ObserveProfileUseCase = mockk()
    private val profileRepo: ProfileRepository = mockk()
    private val observeConversations: ObserveConversationsUseCase = mockk()
    private val refreshInbox: RefreshInboxUseCase = mockk()
    private val refreshStats: RefreshDashboardUseCase = mockk()

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getStats() } returns flowOf(null)
        every { observeProfile() } returns flowOf(null)
        every { profileRepo.observeTenant() } returns flowOf(null)
        coEvery { profileRepo.refreshProfile() } returns ApiResult.Success(Unit)
        coEvery { profileRepo.refreshTenant() } returns ApiResult.Success(Unit)
        coEvery { refreshInbox() } returns ApiResult.Success(Unit)
        coEvery { refreshStats() } returns ApiResult.Success(Unit)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun conversation(id: String, unread: Int, lastMessageAt: Instant) = Conversation(
        id = id,
        contactId = "c-$id",
        contactName = "Contact $id",
        contactPhone = "+9199000000$id",
        avatarUrl = null,
        lastMessage = "hi",
        lastMessageAt = lastMessageAt,
        lastMessageStatus = MessageStatus.DELIVERED,
        lastMessageOutbound = false,
        unreadCount = unread,
        isPinned = false,
        isMuted = false
    )

    @Test
    fun `pending replies counts conversations with unread messages`() = runTest {
        val now = Instant.now()
        every { observeConversations("") } returns flowOf(
            listOf(
                conversation("1", unread = 3, lastMessageAt = now),
                conversation("2", unread = 0, lastMessageAt = now),
                conversation("3", unread = 1, lastMessageAt = now)
            )
        )

        val vm = newViewModel()

        assertThat(vm.state.value.pendingReplies).isEqualTo(2)
        assertThat(vm.state.value.hasUnreadConversations).isTrue()
    }

    @Test
    fun `conversations today excludes older conversations`() = runTest {
        val now = Instant.now()
        every { observeConversations("") } returns flowOf(
            listOf(
                conversation("1", unread = 0, lastMessageAt = now),
                conversation("2", unread = 0, lastMessageAt = now.minus(3, ChronoUnit.DAYS))
            )
        )

        val vm = newViewModel()

        assertThat(vm.state.value.conversationsToday).isEqualTo(1)
    }

    @Test
    fun `no conversations means nothing pending and no unread dot`() = runTest {
        every { observeConversations("") } returns flowOf(emptyList())

        val vm = newViewModel()

        assertThat(vm.state.value.pendingReplies).isEqualTo(0)
        assertThat(vm.state.value.conversationsToday).isEqualTo(0)
        assertThat(vm.state.value.hasUnreadConversations).isFalse()
    }

    private fun newViewModel() = DashboardViewModel(
        getStats = getStats,
        observeProfile = observeProfile,
        profileRepo = profileRepo,
        observeConversations = observeConversations,
        refreshInbox = refreshInbox,
        refreshStats = refreshStats
    )
}
