package com.chain.app.domain.repository

import com.chain.app.domain.model.MessageTransaction
import com.chain.app.domain.model.NetworkEvent
import com.chain.app.domain.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for blockchain operations.
 */
interface BlockchainRepository {
    /**
     * Connect to the blockchain network.
     */
    suspend fun connect(nodeUrl: String): Result<Unit>

    /**
     * Send a message transaction to the blockchain.
     */
    suspend fun sendTransaction(transaction: MessageTransaction): Result<String>

    /**
     * Subscribe to incoming messages for the current user.
     */
    fun subscribeToMessages(): Flow<MessageTransaction>

    /**
     * Get network status.
     */
    fun getNetworkStatus(): Flow<NetworkStatus>

    /**
     * Prune old messages from the blockchain.
     */
    suspend fun pruneOldMessages(olderThan: Long): Result<Unit>

    /**
     * Sync with the blockchain network.
     */
    suspend fun syncBlockchain(): Result<Unit>

    /**
     * Observe network events.
     */
    fun observeNetworkEvents(): Flow<NetworkEvent>

    /**
     * Disconnect from the blockchain network.
     */
    suspend fun disconnect(): Result<Unit>
}
