package com.chain.app.domain.usecase.auth

import com.chain.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to get the current authenticated user's ID.
 * Returns null if no user is authenticated.
 */
class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        return authRepository.getCurrentUserId()
    }
}
