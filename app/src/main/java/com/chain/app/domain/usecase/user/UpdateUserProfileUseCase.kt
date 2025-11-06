package com.chain.app.domain.usecase.user

import com.chain.app.domain.model.User
import com.chain.app.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case to update the current user's profile.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        displayName: String? = null,
        avatar: String? = null
    ): Result<User> {
        // Validation
        if (displayName != null && displayName.isBlank()) {
            return Result.failure(Exception("Display name cannot be empty"))
        }

        if (displayName != null && displayName.length < 2) {
            return Result.failure(Exception("Display name must be at least 2 characters"))
        }

        return userRepository.updateProfile(
            displayName = displayName?.trim(),
            avatar = avatar
        )
    }
}
