package com.chain.app.domain.usecase.p2p

import com.chain.app.domain.model.NetworkInfo
import com.chain.app.domain.repository.P2PRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for starting the P2P network.
 * Initializes WebRTC, DHT, mDNS, and signaling services.
 */
class StartP2PNetworkUseCase @Inject constructor(
    private val p2pRepository: P2PRepository
) {
    suspend operator fun invoke(): Result<NetworkInfo> {
        return try {
            Timber.i("Starting P2P network...")
            val networkInfo = p2pRepository.startNode().getOrThrow()
            Timber.i("P2P network started successfully: ${networkInfo.localPeerId}")
            Result.success(networkInfo)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start P2P network")
            Result.failure(e)
        }
    }
}
