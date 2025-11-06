package com.chain.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Chat
import com.chain.app.domain.usecase.GetChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the chat list screen.
 * Demonstrates MVVM architecture with Clean Architecture.
 * Enhanced with search functionality.
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase
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
}

/**
 * UI state for the chat list screen.
 */
sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val chats: List<Chat>) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}
