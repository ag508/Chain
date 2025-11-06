package com.chain.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.User
import com.chain.app.domain.usecase.user.GetCurrentUserUseCase
import com.chain.app.domain.usecase.user.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    _uiState.value = ProfileUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = ProfileUiState.Error(
                        error.message ?: "Failed to load profile"
                    )
                }
            )
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun updateProfile(
        displayName: String? = null,
        avatar: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            updateUserProfileUseCase(
                displayName = displayName,
                avatar = avatar
            ).fold(
                onSuccess = { updatedUser ->
                    _uiState.value = ProfileUiState.Success(updatedUser)
                    _isEditing.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    // Reload the original profile
                    loadProfile()
                    onError(error.message ?: "Failed to update profile")
                }
            )
        }
    }
}
