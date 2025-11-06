package com.chain.app.domain.usecase.call

import com.chain.app.domain.repository.CallRepository
import javax.inject.Inject

/**
 * Use case to reject an incoming call.
 */
class RejectCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(callId: String): Result<Unit> {
        // Validation
        if (callId.isBlank()) {
            return Result.failure(Exception("Call ID cannot be empty"))
        }

        return callRepository.rejectCall(callId)
    }
}
