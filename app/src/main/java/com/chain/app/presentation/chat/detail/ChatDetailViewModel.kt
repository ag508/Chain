package com.chain.app.presentation.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.Message
import com.chain.app.domain.usecase.GetChatByIdUseCase
import com.chain.app.domain.usecase.GetMessagesForChatUseCase
import com.chain.app.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chat detail screen
 */
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val getMessagesForChatUseCase: GetMessagesForChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentChatId: String = ""
    private var currentUserId: String = "" // TODO: Get from auth

    fun loadChat(chatId: String) {
        currentChatId = chatId

        // Load chat info in separate coroutine
        viewModelScope.launch {
            try {
                getChatByIdUseCase(chatId).collect { chat ->
                    _uiState.value = ChatDetailUiState.Success(chat)
                }
            } catch (e: Exception) {
                _uiState.value = ChatDetailUiState.Error(e.message ?: "Failed to load chat")
            }
        }

        // Load messages in separate coroutine so it doesn't block
        viewModelScope.launch {
            try {
                getMessagesForChatUseCase(chatId).collect { messageList ->
                    _messages.value = messageList.sortedBy { it.timestamp }
                }
            } catch (e: Exception) {
                // Messages failed to load, but keep the chat info
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                sendMessageUseCase(
                    chatId = currentChatId,
                    content = content
                )
            } catch (e: Exception) {
                // TODO: Show error to user
            }
        }
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }
}

/**
 * UI state for chat detail screen
 */
sealed class ChatDetailUiState {
    object Loading : ChatDetailUiState()
    data class Success(val chat: Chat) : ChatDetailUiState()
    data class Error(val message: String) : ChatDetailUiState()
}
