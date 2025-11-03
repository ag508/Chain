package com.chain.app.domain.usecase.auth

import javax.inject.Inject

/**
 * Use case to validate OTP code.
 */
class ValidateOtpUseCase @Inject constructor() {

    operator fun invoke(otp: String): ValidationResult {
        if (otp.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "OTP cannot be empty"
            )
        }

        val digitsOnly = otp.filter { it.isDigit() }

        if (digitsOnly.length != 6) {
            return ValidationResult(
                successful = false,
                errorMessage = "OTP must be 6 digits"
            )
        }

        return ValidationResult(successful = true)
    }

    data class ValidationResult(
        val successful: Boolean,
        val errorMessage: String? = null
    )
}
