package com.chain.app.presentation.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.usecase.auth.ValidatePhoneNumberUseCase
import com.chain.app.domain.usecase.auth.ValidatePhoneNumberUseCase.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for phone number entry screen.
 */
@HiltViewModel
class PhoneNumberViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validatePhoneNumber: ValidatePhoneNumberUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneNumberState())
    val state = _state.asStateFlow()

    fun onPhoneNumberChanged(phoneNumber: String) {
        _state.update { it.copy(phoneNumber = phoneNumber, phoneNumberError = null) }
    }

    fun onCountryCodeChanged(countryCode: String) {
        _state.update { it.copy(countryCode = countryCode) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onContinueClick() {
        // Validate phone number
        val phoneValidation = validatePhoneNumber(_state.value.phoneNumber)
        if (!phoneValidation.successful) {
            _state.update { it.copy(phoneNumberError = phoneValidation.errorMessage) }
            return
        }

        // Validate email
        val emailValidation = validateEmail(_state.value.email)
        if (!emailValidation.successful) {
            _state.update { it.copy(emailError = emailValidation.errorMessage) }
            return
        }

        sendOtp()
    }

    private fun sendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val fullPhoneNumber = "${_state.value.countryCode}${_state.value.phoneNumber}"
            val email = _state.value.email
            val result = authRepository.sendOtp(fullPhoneNumber, email)

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        otpSent = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                    )
                }
            }
        }
    }

    private fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Email is required")
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!email.matches(emailRegex)) {
            return ValidationResult(successful = false, errorMessage = "Invalid email address")
        }

        return ValidationResult(successful = true)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class PhoneNumberState(
    val phoneNumber: String = "",
    val countryCode: String = "+1",
    val email: String = "",
    val phoneNumberError: String? = null,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false
)
