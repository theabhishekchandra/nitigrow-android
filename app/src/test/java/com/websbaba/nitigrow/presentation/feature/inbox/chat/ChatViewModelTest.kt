package com.websbaba.nitigrow.presentation.feature.inbox.chat

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.notifications.ForegroundChatTracker
import com.websbaba.nitigrow.core.notifications.NotificationPresenter
import com.websbaba.nitigrow.domain.model.Conversation
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.model.MessageStatus
import com.websbaba.nitigrow.domain.usecase.chat.MarkConversationReadUseCase
import com.websbaba.nitigrow.domain.usecase.chat.ObserveTypingUseCase
import com.websbaba.nitigrow.domain.usecase.chat.PagedMessagesUseCase
import com.websbaba.nitigrow.domain.usecase.chat.RetryMessageUseCase
import com.websbaba.nitigrow.domain.usecase.chat.SendMessageUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val typingFlow = MutableStateFlow(false)
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())

    private val pagedMessages: PagedMessagesUseCase = mockk()
    private val observeTyping: ObserveTypingUseCase = mockk()
    private val observeConversations: ObserveConversationsUseCase = mockk()
    private val sendMessage: SendMessageUseCase = mockk()
    private val retryMessage: RetryMessageUseCase = mockk()
    private val markRead: MarkConversationReadUseCase = mockk()
    private val foregroundChat = ForegroundChatTracker()
    private val notifications: NotificationPresenter = mockk(relaxed = true)
    private lateinit var vm: ChatViewModel

    private val lastInboundAt = Instant.parse("2026-06-13T10:00:00Z")

    private val conversation = Conversation(
        id = CONVERSATION_ID,
        contactId = "contact-1",
        contactName = "Kavita Reddy",
        contactPhone = "9876543210",
        avatarUrl = null,
        lastMessage = "Hi, is the gift hamper available?",
        lastMessageAt = lastInboundAt,
        lastMessageStatus = MessageStatus.DELIVERED,
        lastMessageOutbound = false,
        unreadCount = 2,
        isPinned = false,
        isMuted = false
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { pagedMessages(CONVERSATION_ID) } returns flowOf(PagingData.empty<Message>())
        every { observeTyping(CONVERSATION_ID) } returns typingFlow
        every { observeConversations("") } returns conversationsFlow
        coEvery { markRead(CONVERSATION_ID) } returns ApiResult.Success(Unit)
        vm = ChatViewModel(
            savedState = SavedStateHandle(mapOf("conversationId" to CONVERSATION_ID)),
            pagedMessages = pagedMessages,
            observeTyping = observeTyping,
            observeConversations = observeConversations,
            sendMessage = sendMessage,
            retryMessage = retryMessage,
            markRead = markRead,
            foregroundChat = foregroundChat,
            notifications = notifications
        )
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // --- Init side effects ---

    @Test
    fun `init marks the conversation read, tracks foreground chat and clears its notification`() = runTest {
        coVerify { markRead(CONVERSATION_ID) }
        assertThat(foregroundChat.isOpen(CONVERSATION_ID)).isTrue()
        verify { notifications.cancel(CONVERSATION_ID) }
    }

    // --- Sending ---

    @Test
    fun `onDraftChange updates the draft`() = runTest {
        vm.onDraftChange("Hello")

        assertThat(vm.state.value.draft).isEqualTo("Hello")
    }

    @Test
    fun `onSend with a blank draft does not call the use case`() = runTest {
        vm.onDraftChange("   ")

        vm.onSend()

        coVerify(exactly = 0) { sendMessage(any(), any()) }
    }

    @Test
    fun `onSend sends the trimmed text and clears the draft optimistically`() = runTest {
        coEvery { sendMessage(CONVERSATION_ID, "Hello") } returns "client-1"
        vm.onDraftChange("  Hello  ")

        vm.onSend()

        assertThat(vm.state.value.draft).isEmpty()
        coVerify { sendMessage(CONVERSATION_ID, "Hello") }
    }

    @Test
    fun `onSend failure surfaces a ShowError effect`() = runTest {
        coEvery { sendMessage(CONVERSATION_ID, "Hello") } throws IllegalStateException("Network down")
        vm.onDraftChange("Hello")

        vm.effects.test {
            vm.onSend()
            assertThat(awaitItem()).isEqualTo(ChatEffect.ShowError("Network down"))
        }
    }

    @Test
    fun `onSend failure without a message falls back to a generic error`() = runTest {
        coEvery { sendMessage(CONVERSATION_ID, "Hello") } throws RuntimeException()
        vm.onDraftChange("Hello")

        vm.effects.test {
            vm.onSend()
            assertThat(awaitItem()).isEqualTo(ChatEffect.ShowError("Send failed"))
        }
    }

    @Test
    fun `onRetry delegates to the retry use case`() = runTest {
        coEvery { retryMessage("client-9") } returns ApiResult.Success(Unit)

        vm.onRetry("client-9")

        coVerify { retryMessage("client-9") }
    }

    // --- Typing indicator ---

    @Test
    fun `typing indicator from the repository streams into state`() = runTest {
        typingFlow.value = true

        assertThat(vm.state.value.typing).isTrue()
    }

    // --- Header + 24h customer-service window ---

    @Test
    fun `inbound last message binds the header and opens the 24h window`() = runTest {
        conversationsFlow.value = listOf(conversation)

        val s = vm.state.value
        assertThat(s.contactName).isEqualTo("Kavita Reddy")
        assertThat(s.contactPhone).isEqualTo("9876543210")
        assertThat(s.windowExpiresAt).isEqualTo(lastInboundAt.plus(Duration.ofHours(24)))
    }

    @Test
    fun `outbound last message hides the 24h window`() = runTest {
        conversationsFlow.value = listOf(conversation.copy(lastMessageOutbound = true))

        assertThat(vm.state.value.windowExpiresAt).isNull()
    }

    @Test
    fun `conversations for other chats leave the header untouched`() = runTest {
        conversationsFlow.value = listOf(conversation.copy(id = "conv-other"))

        assertThat(vm.state.value.contactName).isEmpty()
        assertThat(vm.state.value.windowExpiresAt).isNull()
    }

    private companion object { const val CONVERSATION_ID = "conv-1" }
}
