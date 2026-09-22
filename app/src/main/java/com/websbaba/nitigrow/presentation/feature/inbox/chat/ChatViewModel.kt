package com.websbaba.nitigrow.presentation.feature.inbox.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.websbaba.nitigrow.core.notifications.ForegroundChatTracker
import com.websbaba.nitigrow.core.notifications.NotificationPresenter
import com.websbaba.nitigrow.domain.model.Message
import com.websbaba.nitigrow.domain.usecase.chat.MarkConversationReadUseCase
import com.websbaba.nitigrow.domain.usecase.chat.ObserveTypingUseCase
import com.websbaba.nitigrow.domain.usecase.chat.PagedMessagesUseCase
import com.websbaba.nitigrow.domain.usecase.chat.RetryMessageUseCase
import com.websbaba.nitigrow.domain.usecase.chat.SendMessageUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import com.websbaba.nitigrow.presentation.feature.inbox.list.replyWindowExpiresAt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedState: SavedStateHandle,
    pagedMessages: PagedMessagesUseCase,
    observeTyping: ObserveTypingUseCase,
    observeConversations: ObserveConversationsUseCase,
    private val sendMessage: SendMessageUseCase,
    private val retryMessage: RetryMessageUseCase,
    private val markRead: MarkConversationReadUseCase,
    private val foregroundChat: ForegroundChatTracker,
    private val notifications: NotificationPresenter
) : BaseViewModel() {

    private val conversationId: String = savedState.get<String>("conversationId").orEmpty()

    private val _state = MutableStateFlow(ChatUiState(conversationId = conversationId))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val messages: Flow<PagingData<Message>> =
        pagedMessages(conversationId).cachedIn(viewModelScope)

    private val typingDraft = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    init {
        observeTyping(conversationId)
            .onEach { typing -> _state.update { it.copy(typing = typing) } }
            .launchIn(viewModelScope)

        // Header binding: contact identity + 24h-window pill, observed from the
        // cached conversation list. The window is only derivable while the last
        // message is inbound (each inbound message restarts the 24h clock).
        observeConversations("")
            .onEach { conversations ->
                val conv = conversations.find { it.id == conversationId } ?: return@onEach
                _state.update {
                    it.copy(
                        contactName = conv.contactName,
                        contactPhone = conv.contactPhone,
                        avatarUrl = conv.avatarUrl,
                        windowExpiresAt = conv.replyWindowExpiresAt()
                    )
                }
            }
            .launchIn(viewModelScope)

        // Throttle typing emit on user keystrokes
        typingDraft
            .debounce(300)
            .onEach { /* delegate to repo via send-typing use case if added */ }
            .launchIn(viewModelScope)

        viewModelScope.launch { markRead(conversationId) }
        foregroundChat.enter(conversationId)
        notifications.cancel(conversationId)
    }

    override fun onCleared() {
        foregroundChat.exit(conversationId)
        super.onCleared()
    }

    fun onDraftChange(value: String) {
        _state.update { it.copy(draft = value) }
        typingDraft.tryEmit(Unit)
    }

    fun onSend() {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        _state.update { it.copy(draft = "") }
        viewModelScope.launch {
            runCatching { sendMessage(conversationId, text) }
                .onFailure { t ->
                    _effects.send(ChatEffect.ShowError(t.message ?: "Send failed"))
                }
        }
    }

    fun onRetry(clientId: String) {
        viewModelScope.launch { retryMessage(clientId) }
    }

}
