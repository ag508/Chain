package com.chain.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Chat
import com.chain.app.domain.usecase.GetChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the chat list screen.
 * Demonstrates MVVM architecture with Clean Architecture.
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChats()
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
                    _uiState.value = ChatListUiState.Success(chats)
                }
        }
    }

    fun refresh() {
        _uiState.value = ChatListUiState.Loading
        loadChats()
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
