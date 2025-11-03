package com.chain.app.presentation.auth.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.usecase.auth.ValidateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for profile setup screen.
 */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateProfile: ValidateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state = _state.asStateFlow()

    fun setUserData(userId: String, phoneNumber: String) {
        _state.update { it.copy(userId = userId, phoneNumber = phoneNumber) }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onProfileImageSelected(uri: Uri?) {
        _state.update { it.copy(profileImageUri = uri) }
    }

    fun onContinueClick() {
        val validation = validateProfile(_state.value.name)

        if (!validation.successful) {
            _state.update { it.copy(nameError = validation.errorMessage) }
            return
        }

        createProfile()
    }

    private fun createProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = _state.value.userId
            val phoneNumber = _state.value.phoneNumber

            if (userId == null || phoneNumber == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "User data is missing. Please try again."
                    )
                }
                return@launch
            }

            // In a real app, we would upload the profile image first
            // For now, we'll use the URI as string or null
            val avatarUrl = _state.value.profileImageUri?.toString()

            val result = authRepository.createUserProfile(
                userId = userId,
                phoneNumber = phoneNumber,
                displayName = _state.value.name,
                avatar = avatarUrl
            )

            if (result.isSuccess) {
                // Initialize encryption keys
                val encryptionResult = authRepository.initializeEncryption()

                if (encryptionResult.isSuccess) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            profileCreated = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to initialize encryption"
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to create profile"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class ProfileSetupState(
    val userId: String? = null,
    val phoneNumber: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val profileCreated: Boolean = false
)
