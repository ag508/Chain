package com.chain.app.domain.usecase.auth

import javax.inject.Inject

/**
 * Use case to validate phone number format.
 */
class ValidatePhoneNumberUseCase @Inject constructor() {

    operator fun invoke(phoneNumber: String): ValidationResult {
        if (phoneNumber.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Phone number cannot be empty"
            )
        }

        // Remove all non-digit characters
        val digitsOnly = phoneNumber.filter { it.isDigit() }

        if (digitsOnly.length < 10) {
            return ValidationResult(
                successful = false,
                errorMessage = "Phone number must be at least 10 digits"
            )
        }

        if (digitsOnly.length > 11) {
            return ValidationResult(
                successful = false,
                errorMessage = "Phone number must not exceed 11 digits"
            )
        }

        return ValidationResult(successful = true)
    }

    data class ValidationResult(
        val successful: Boolean,
        val errorMessage: String? = null
    )
}
