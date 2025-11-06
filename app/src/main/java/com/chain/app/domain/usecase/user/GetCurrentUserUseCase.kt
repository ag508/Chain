package com.chain.app.domain.usecase.user

import com.chain.app.domain.model.User
import com.chain.app.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case to get the current authenticated user's profile.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.getCurrentUser()
    }
}
