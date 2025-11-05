package com.chain.app.domain.usecase.p2p

import com.chain.app.domain.repository.P2PRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for stopping the P2P network.
 * Cleanly shuts down all P2P components.
 */
class StopP2PNetworkUseCase @Inject constructor(
    private val p2pRepository: P2PRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            Timber.i("Stopping P2P network...")
            p2pRepository.stopNode().getOrThrow()
            Timber.i("P2P network stopped successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop P2P network")
            Result.failure(e)
        }
    }
}
