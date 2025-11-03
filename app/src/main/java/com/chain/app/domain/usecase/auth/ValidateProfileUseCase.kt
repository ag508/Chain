package com.chain.app.domain.usecase.auth

import javax.inject.Inject

/**
 * Use case to validate user profile information.
 */
class ValidateProfileUseCase @Inject constructor() {

    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name cannot be empty"
            )
        }

        if (name.length < 2) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name must be at least 2 characters"
            )
        }

        if (name.length > 50) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name is too long"
            )
        }

        // Check if name contains only valid characters (letters, spaces, some punctuation)
        val validNameRegex = "^[a-zA-Z\\s'-]+$".toRegex()
        if (!name.matches(validNameRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name contains invalid characters"
            )
        }

        return ValidationResult(successful = true)
    }

    data class ValidationResult(
        val successful: Boolean,
        val errorMessage: String? = null
    )
}
