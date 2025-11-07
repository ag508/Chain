package com.chain.app.presentation.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.repository.AuthRepository
import com.chain.app.domain.usecase.auth.ValidateOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for OTP verification screen.
 */
@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateOtp: ValidateOtpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val phoneNumber: String = savedStateHandle.get<String>("phoneNumber") ?: ""
    private val email: String = savedStateHandle.get<String>("email") ?: ""

    private val _state = MutableStateFlow(OtpState(phoneNumber = phoneNumber))
    val state = _state.asStateFlow()

    fun onOtpChanged(otp: String) {
        if (otp.length <= 6) {
            _state.update { it.copy(otp = otp, otpError = null) }

            // Auto-verify when 6 digits entered
            if (otp.length == 6) {
                verifyOtp()
            }
        }
    }

    fun onVerifyClick() {
        verifyOtp()
    }

    fun onResendClick() {
        resendOtp()
    }

    private fun verifyOtp() {
        val validation = validateOtp(_state.value.otp)

        if (!validation.successful) {
            _state.update { it.copy(otpError = validation.errorMessage) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.verifyOtp(phoneNumber, _state.value.otp)

            if (result.isSuccess) {
                val userId = result.getOrNull()!!
                _state.update {
                    it.copy(
                        isLoading = false,
                        verified = true,
                        userId = userId
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Invalid OTP",
                        otp = "" // Clear OTP on error
                    )
                }
            }
        }
    }

    private fun resendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.sendOtp(phoneNumber, email)

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        otp = "",
                        otpError = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to resend OTP"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class OtpState(
    val phoneNumber: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val verified: Boolean = false,
    val userId: String? = null
)
