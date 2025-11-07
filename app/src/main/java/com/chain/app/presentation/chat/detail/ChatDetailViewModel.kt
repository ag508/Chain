package com.chain.app.presentation.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.MessageType
import com.chain.app.domain.repository.MessageRepository
import com.chain.app.domain.usecase.GetChatByIdUseCase
import com.chain.app.domain.usecase.GetMessagesForChatUseCase
import com.chain.app.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for chat detail screen
 */
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val getMessagesForChatUseCase: GetMessagesForChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentChatId: String = ""
    private var currentUserId: String = ""

    init {
        // Load current user ID
        viewModelScope.launch {
            currentUserId = userPreferences.getUserId() ?: ""
            if (currentUserId.isEmpty()) {
                Timber.w("No user ID found in preferences")
            }
        }
    }

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
        if (content.isBlank() || currentChatId.isEmpty() || currentUserId.isEmpty()) return

        viewModelScope.launch {
            try {
                sendMessageUseCase(
                    chatId = currentChatId,
                    content = content,
                    senderId = currentUserId
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message")
            }
        }
    }

    fun sendImageMessage(uri: android.net.Uri, caption: String = "") {
        if (currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = caption,
                    type = MessageType.IMAGE,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "uri" to uri.toString(),
                        "fileName" to uri.lastPathSegment,
                        "caption" to caption
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send image message")
            }
        }
    }

    fun sendDocumentMessage(uri: android.net.Uri, fileName: String) {
        if (currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = fileName,
                    type = MessageType.DOCUMENT,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "uri" to uri.toString(),
                        "fileName" to fileName
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send document message")
            }
        }
    }

    fun sendLocationMessage(latitude: Double, longitude: Double, address: String = "") {
        if (currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = address.ifEmpty { "Location: $latitude, $longitude" },
                    type = MessageType.LOCATION,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "address" to address
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send location message")
            }
        }
    }

    fun sendPollMessage(question: String, options: List<String>) {
        if (currentChatId.isEmpty() || question.isBlank() || options.size < 2) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = question,
                    type = MessageType.POLL,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "options" to options,
                        "votes" to options.associate { it to 0 }
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send poll message")
            }
        }
    }

    fun sendVideoMessage(uri: android.net.Uri, caption: String = "", duration: Long = 0) {
        if (currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = caption,
                    type = MessageType.VIDEO,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "uri" to uri.toString(),
                        "fileName" to uri.lastPathSegment,
                        "caption" to caption,
                        "duration" to duration
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send video message")
            }
        }
    }

    fun sendAudioMessage(uri: android.net.Uri, duration: Long = 0) {
        if (currentChatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = "Audio message (${duration}s)",
                    type = MessageType.AUDIO,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "uri" to uri.toString(),
                        "fileName" to uri.lastPathSegment,
                        "duration" to duration
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send audio message")
            }
        }
    }

    fun sendContactMessage(name: String, phoneNumber: String, email: String? = null) {
        if (currentChatId.isEmpty() || name.isBlank() || phoneNumber.isBlank()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = currentChatId,
                    senderId = currentUserId,
                    content = name,
                    type = MessageType.CONTACT,
                    timestamp = java.util.Date(),
                    status = MessageStatus.SENDING,
                    metadata = mapOf(
                        "name" to name,
                        "phoneNumber" to phoneNumber,
                        "email" to (email ?: "")
                    )
                )
                messageRepository.sendMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send contact message")
            }
        }
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
