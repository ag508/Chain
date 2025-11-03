package com.chain.app.presentation.auth.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for biometric setup screen.
 */
@HiltViewModel
class BiometricSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BiometricSetupState())
    val state = _state.asStateFlow()

    fun onEnableBiometricClick() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.enableBiometric()

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        biometricEnabled = true,
                        setupComplete = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to enable biometric"
                    )
                }
            }
        }
    }

    fun onSkipClick() {
        _state.update { it.copy(setupComplete = true) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class BiometricSetupState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val biometricEnabled: Boolean = false,
    val setupComplete: Boolean = false
)
