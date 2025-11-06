package com.chain.app.domain.usecase.call

import com.chain.app.domain.repository.CallRepository
import javax.inject.Inject

/**
 * Use case to end an active call.
 */
class EndCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(callId: String): Result<Unit> {
        // Validation
        if (callId.isBlank()) {
            return Result.failure(Exception("Call ID cannot be empty"))
        }

        return callRepository.endCall(callId)
    }
}
