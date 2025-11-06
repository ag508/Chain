package com.chain.app.presentation.groups

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.GroupChat
import com.chain.app.domain.usecase.group.CreateGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for group setup screen.
 * Handles group creation logic.
 */
@HiltViewModel
class GroupSetupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupSetupUiState>(GroupSetupUiState.Idle)
    val uiState: StateFlow<GroupSetupUiState> = _uiState.asStateFlow()

    /**
     * Create a new group chat with the given details.
     *
     * @param groupName Name of the group
     * @param selectedContactIds List of contact/user IDs to add
     * @param groupIcon Optional icon identifier
     * @param onSuccess Callback when group is created successfully with the groupChat
     * @param onError Callback when group creation fails with error message
     */
    fun createGroup(
        groupName: String,
        selectedContactIds: List<String>,
        groupIcon: String? = null,
        onSuccess: (GroupChat) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Log.d("GroupSetupViewModel", "createGroup called with name: $groupName, members: ${selectedContactIds.size}")

        viewModelScope.launch {
            try {
                _uiState.value = GroupSetupUiState.Loading

                // Create the group
                val result = createGroupUseCase(
                    name = groupName,
                    participants = selectedContactIds,
                    description = null // Can be added later if needed
                )

                result.fold(
                    onSuccess = { groupChat ->
                        Log.d("GroupSetupViewModel", "Group created successfully: ${groupChat.chat.id}")
                        _uiState.value = GroupSetupUiState.Success(groupChat)
                        onSuccess(groupChat)
                    },
                    onFailure = { error ->
                        Log.e("GroupSetupViewModel", "Failed to create group", error)
                        val errorMsg = error.message ?: "Failed to create group"
                        _uiState.value = GroupSetupUiState.Error(errorMsg)
                        onError(errorMsg)
                    }
                )
            } catch (e: Exception) {
                Log.e("GroupSetupViewModel", "Exception creating group", e)
                val errorMsg = e.message ?: "An error occurred"
                _uiState.value = GroupSetupUiState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }

    /**
     * Reset the UI state to idle.
     */
    fun resetState() {
        _uiState.value = GroupSetupUiState.Idle
    }
}

/**
 * UI state for group setup screen.
 */
sealed class GroupSetupUiState {
    object Idle : GroupSetupUiState()
    object Loading : GroupSetupUiState()
    data class Success(val groupChat: GroupChat) : GroupSetupUiState()
    data class Error(val message: String) : GroupSetupUiState()
}
