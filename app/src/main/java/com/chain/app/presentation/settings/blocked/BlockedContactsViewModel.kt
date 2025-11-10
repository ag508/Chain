package com.chain.app.presentation.settings.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BlockedContactsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedContactsUiState>(BlockedContactsUiState.Loading)
    val uiState: StateFlow<BlockedContactsUiState> = _uiState.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers.asStateFlow()

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = BlockedContactsUiState.Loading

                userRepository.getBlockedUsers().collect { users ->
                    val blockedUsersList = users.map { user ->
                        BlockedUser(
                            id = user.id,
                            name = user.displayName,
                            blockedDate = formatDate(user.blockedAt ?: Date())
                        )
                    }
                    _blockedUsers.value = blockedUsersList
                    _uiState.value = BlockedContactsUiState.Success
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load blocked users")
                _uiState.value = BlockedContactsUiState.Error("Failed to load blocked contacts")
            }
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.unblockUser(userId).fold(
                    onSuccess = {
                        Timber.d("User $userId unblocked successfully")
                        // List will update automatically through the flow
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to unblock user")
                        _uiState.value = BlockedContactsUiState.Error("Failed to unblock user")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to unblock user")
                _uiState.value = BlockedContactsUiState.Error("Failed to unblock user")
            }
        }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}

sealed class BlockedContactsUiState {
    object Loading : BlockedContactsUiState()
    object Success : BlockedContactsUiState()
    data class Error(val message: String) : BlockedContactsUiState()
}
