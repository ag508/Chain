package com.chain.app.domain.usecase.p2p

import com.chain.app.domain.model.NetworkStatus
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing network status.
 * Provides real-time updates on peer connections and network health.
 */
class GetNetworkStatusUseCase @Inject constructor(
    private val p2pRepository: P2PRepository
) {
    operator fun invoke(): Flow<NetworkStatus> {
        return p2pRepository.getNetworkStatus()
    }
}
