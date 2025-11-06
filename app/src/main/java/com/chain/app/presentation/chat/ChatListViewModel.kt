package com.chain.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Chat
import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.ChatRepository
import com.chain.app.domain.usecase.GetChatsUseCase
import com.chain.app.domain.usecase.auth.GetCurrentUserIdUseCase
import com.chain.app.domain.usecase.contact.AddContactUseCase
import com.chain.app.domain.usecase.contact.SearchContactByPhoneUseCase
import com.chain.app.domain.usecase.debug.SeedSampleDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the chat list screen.
 * Demonstrates MVVM architecture with Clean Architecture.
 * Enhanced with search functionality and contact management.
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase,
    private val searchContactByPhoneUseCase: SearchContactByPhoneUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val chatRepository: ChatRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val seedSampleDataUseCase: SeedSampleDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allChats = MutableStateFlow<List<Chat>>(emptyList())

    init {
        loadChats()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            combine(_allChats, _searchQuery) { chats, query ->
                if (query.isBlank()) {
                    chats
                } else {
                    chats.filter { chat ->
                        chat.name.contains(query, ignoreCase = true) ||
                        chat.lastMessage?.content?.contains(query, ignoreCase = true) == true
                    }
                }
            }.collect { filteredChats ->
                if (_uiState.value !is ChatListUiState.Loading) {
                    _uiState.value = ChatListUiState.Success(filteredChats)
                }
            }
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            getChatsUseCase()
                .catch { exception ->
                    _uiState.value = ChatListUiState.Error(
                        exception.message ?: "Failed to load chats"
                    )
                }
                .collect { chats ->
                    _allChats.value = chats
                    // Apply current search filter
                    val query = _searchQuery.value
                    val filteredChats = if (query.isBlank()) {
                        chats
                    } else {
                        chats.filter { chat ->
                            chat.name.contains(query, ignoreCase = true) ||
                            chat.lastMessage?.content?.contains(query, ignoreCase = true) == true
                        }
                    }
                    _uiState.value = ChatListUiState.Success(filteredChats)
                }
        }
    }

    fun refresh() {
        _uiState.value = ChatListUiState.Loading
        loadChats()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Search for a contact by phone number and add them if found.
     * Creates a direct chat with the contact after adding.
     */
    fun addContactByPhone(phoneNumber: String, onSuccess: (Chat) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Search for contact
                val contact = searchContactByPhoneUseCase(phoneNumber)

                if (contact == null) {
                    onError("No user found with this phone number")
                    return@launch
                }

                // Add contact to local list
                addContactUseCase(
                    userId = contact.userId,
                    phoneNumber = contact.phoneNumber,
                    displayName = contact.displayName,
                    avatar = contact.avatar,
                    publicKey = contact.publicKey
                ).fold(
                    onSuccess = {
                        // Create direct chat with the contact
                        createDirectChat(contact, onSuccess, onError)
                    },
                    onFailure = { error ->
                        if (error.message?.contains("already exists") == true) {
                            // Contact already exists, just create/open chat
                            createDirectChat(contact, onSuccess, onError)
                        } else {
                            onError(error.message ?: "Failed to add contact")
                        }
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Create a direct chat with a contact.
     */
    private fun createDirectChat(contact: Contact, onSuccess: (Chat) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            chatRepository.createDirectChat(contact.userId).fold(
                onSuccess = { chat ->
                    onSuccess(chat)
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to create chat")
                }
            )
        }
    }

    /**
     * Seed sample data for testing (debug function).
     * Creates sample contacts, chats, and messages.
     */
    fun seedSampleData(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserIdUseCase()
                if (userId != null) {
                    seedSampleDataUseCase(userId)
                    onSuccess()
                } else {
                    onError("User not authenticated")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to seed data")
            }
        }
    }
}

/**
 * UI state for the chat list screen.
 */
sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val chats: List<Chat>) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}
