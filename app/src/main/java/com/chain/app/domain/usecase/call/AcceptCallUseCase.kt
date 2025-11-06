package com.chain.app.domain.usecase.call

import com.chain.app.domain.model.CallSession
import com.chain.app.domain.repository.CallRepository
import javax.inject.Inject

/**
 * Use case to accept an incoming call.
 */
class AcceptCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(callId: String): Result<CallSession> {
        // Validation
        if (callId.isBlank()) {
            return Result.failure(Exception("Call ID cannot be empty"))
        }

        return callRepository.acceptCall(callId)
    }
}
