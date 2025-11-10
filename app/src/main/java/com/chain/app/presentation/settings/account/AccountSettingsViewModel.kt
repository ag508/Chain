package com.chain.app.presentation.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.UserRepository
import com.chain.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountSettingsUiState>(AccountSettingsUiState.Loading)
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
    val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()

    init {
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            try {
                _uiState.value = AccountSettingsUiState.Loading

                userRepository.getCurrentUser().fold(
                    onSuccess = { user ->
                        _accountInfo.value = AccountInfo(
                            userId = user.id,
                            phoneNumber = user.phoneNumber,
                            email = user.email ?: "",
                            displayName = user.displayName,
                            about = user.about ?: "",
                            avatar = user.avatar
                        )
                        _uiState.value = AccountSettingsUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = AccountSettingsUiState.Error(
                            error.message ?: "Failed to load account info"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AccountSettingsUiState.Error("Failed to load account info")
            }
        }
    }

    fun updateDisplayName(newName: String) {
        if (newName.isBlank()) {
            _uiState.value = AccountSettingsUiState.Error("Name cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AccountSettingsUiState.Loading

                userRepository.updateProfile(displayName = newName).fold(
                    onSuccess = { user ->
                        _accountInfo.value = _accountInfo.value?.copy(displayName = user.displayName)
                        _uiState.value = AccountSettingsUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = AccountSettingsUiState.Error(
                            error.message ?: "Failed to update name"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AccountSettingsUiState.Error("Failed to update name")
            }
        }
    }

    fun updateAbout(newAbout: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AccountSettingsUiState.Loading

                userRepository.updateProfile(about = newAbout).fold(
                    onSuccess = { user ->
                        _accountInfo.value = _accountInfo.value?.copy(about = user.about ?: "")
                        _uiState.value = AccountSettingsUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = AccountSettingsUiState.Error(
                            error.message ?: "Failed to update about"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AccountSettingsUiState.Error("Failed to update about")
            }
        }
    }

    fun refreshAccountInfo() {
        loadAccountInfo()
    }
}

data class AccountInfo(
    val userId: String,
    val phoneNumber: String,
    val email: String,
    val displayName: String,
    val about: String,
    val avatar: String?
)

sealed class AccountSettingsUiState {
    object Loading : AccountSettingsUiState()
    object Success : AccountSettingsUiState()
    data class Error(val message: String) : AccountSettingsUiState()
}
