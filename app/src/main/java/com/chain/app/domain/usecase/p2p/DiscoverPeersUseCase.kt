package com.chain.app.domain.usecase.p2p

import com.chain.app.domain.model.Peer
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for discovering peers on both local and global networks.
 * Combines mDNS (local) and DHT (global) discovery.
 */
class DiscoverPeersUseCase @Inject constructor(
    private val p2pRepository: P2PRepository
) {
    suspend operator fun invoke(): Flow<Peer> {
        Timber.d("Starting peer discovery...")

        // Merge local and global peer discovery
        val localPeers = p2pRepository.discoverLocalPeers()
        val globalPeers = p2pRepository.discoverGlobalPeers()

        return merge(localPeers, globalPeers)
    }
}
