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

    fun setUserData(userId: String, phoneNumber: String, email: String) {
        _state.update { it.copy(userId = userId, phoneNumber = phoneNumber, email = email) }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onProfileImageSelected(uri: Uri?) {
        _state.update { it.copy(profileImageUri = uri) }
    }

    fun onContinueClick() {
        println("DEBUG ProfileSetup: onContinueClick called, name='${_state.value.name}'")
        val validation = validateProfile(_state.value.name)

        if (!validation.successful) {
            println("DEBUG ProfileSetup: Validation failed: ${validation.errorMessage}")
            _state.update { it.copy(nameError = validation.errorMessage) }
            return
        }

        println("DEBUG ProfileSetup: Validation successful, creating profile")
        createProfile()
    }

    private fun createProfile() {
        viewModelScope.launch {
            println("DEBUG ProfileSetup: createProfile started")
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = _state.value.userId
            val phoneNumber = _state.value.phoneNumber
            val email = _state.value.email

            println("DEBUG ProfileSetup: userId=$userId, phoneNumber=$phoneNumber, email=$email")

            if (userId == null || phoneNumber == null || email == null) {
                println("DEBUG ProfileSetup: User data missing!")
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
            println("DEBUG ProfileSetup: avatarUrl=$avatarUrl")

            val result = authRepository.createUserProfile(
                userId = userId,
                phoneNumber = phoneNumber,
                email = email,
                displayName = _state.value.name,
                avatar = avatarUrl
            )

            println("DEBUG ProfileSetup: createUserProfile result: ${result.isSuccess}")

            if (result.isSuccess) {
                // Initialize encryption keys with timeout
                println("DEBUG ProfileSetup: Initializing encryption...")
                val encryptionResult = try {
                    kotlinx.coroutines.withTimeout(10000L) { // 10 second timeout
                        authRepository.initializeEncryption()
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    println("DEBUG ProfileSetup: Encryption initialization timed out after 10 seconds")
                    Result.failure<Unit>(Exception("Encryption initialization timed out"))
                }

                println("DEBUG ProfileSetup: Encryption result: ${encryptionResult.isSuccess}")

                if (encryptionResult.isSuccess) {
                    println("DEBUG ProfileSetup: Profile created successfully with encryption! Setting profileCreated=true")
                } else {
                    println("DEBUG ProfileSetup: Encryption initialization failed: ${encryptionResult.exceptionOrNull()?.message}")
                    println("DEBUG ProfileSetup: Proceeding anyway - encryption can be initialized later")
                }

                // Proceed regardless of encryption result (encryption can be initialized later)
                println("DEBUG ProfileSetup: Setting profileCreated=true")
                _state.update {
                    it.copy(
                        isLoading = false,
                        profileCreated = true
                    )
                }
            } else {
                println("DEBUG ProfileSetup: Profile creation failed: ${result.exceptionOrNull()?.message}")
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
    val email: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val profileCreated: Boolean = false
)
