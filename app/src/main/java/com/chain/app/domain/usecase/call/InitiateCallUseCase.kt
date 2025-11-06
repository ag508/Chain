package com.chain.app.domain.usecase.call

import com.chain.app.domain.model.CallSession
import com.chain.app.domain.model.CallType
import com.chain.app.domain.repository.CallRepository
import javax.inject.Inject

/**
 * Use case to initiate a call with a peer.
 */
class InitiateCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(
        peerId: String,
        callType: CallType
    ): Result<CallSession> {
        // Validation
        if (peerId.isBlank()) {
            return Result.failure(Exception("Peer ID cannot be empty"))
        }

        return callRepository.initiateCall(peerId, callType)
    }
}
