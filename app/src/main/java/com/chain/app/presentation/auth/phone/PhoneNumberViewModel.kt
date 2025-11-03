package com.chain.app.presentation.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.usecase.auth.ValidatePhoneNumberUseCase
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

    fun onContinueClick() {
        val validation = validatePhoneNumber(_state.value.phoneNumber)

        if (!validation.successful) {
            _state.update { it.copy(phoneNumberError = validation.errorMessage) }
            return
        }

        sendOtp()
    }

    private fun sendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val fullPhoneNumber = "${_state.value.countryCode}${_state.value.phoneNumber}"
            val result = authRepository.sendOtp(fullPhoneNumber)

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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class PhoneNumberState(
    val phoneNumber: String = "",
    val countryCode: String = "+1",
    val phoneNumberError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false
)
