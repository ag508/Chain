package com.chain.app.domain.repository

import com.chain.app.domain.model.NetworkEvent
import com.chain.app.domain.model.NetworkInfo
import com.chain.app.domain.model.NetworkStatus
import com.chain.app.domain.model.P2PMessage
import com.chain.app.domain.model.Peer
import com.chain.app.domain.model.PeerConnection
import com.chain.app.domain.model.PeerStatus
import com.chain.app.domain.model.StoredMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for P2P networking operations.
 * Handles peer discovery, direct messaging, and offline message delivery.
 */
interface P2PRepository {

    // ========== Node Lifecycle ==========

    /**
     * Start the P2P node and begin peer discovery.
     * Initializes DHT, mDNS, and WebRTC managers.
     */
    suspend fun startNode(): Result<NetworkInfo>

    /**
     * Stop the P2P node and disconnect from all peers.
     */
    suspend fun stopNode(): Result<Unit>

    /**
     * Get current network status.
     */
    fun getNetworkStatus(): Flow<NetworkStatus>

    /**
     * Get local node information.
     */
    suspend fun getNodeInfo(): NetworkInfo

    // ========== Peer Discovery ==========

    /**
     * Discover peers on the local network (mDNS).
     */
    suspend fun discoverLocalPeers(): Flow<Peer>

    /**
     * Discover peers globally (DHT).
     */
    suspend fun discoverGlobalPeers(): Flow<Peer>

    /**
     * Find a specific peer by their ID.
     */
    suspend fun findPeer(peerId: String): Result<Peer>

    /**
     * Get all currently connected peers.
     */
    fun getConnectedPeers(): Flow<List<PeerConnection>>

    /**
     * Get peer status.
     */
    suspend fun getPeerStatus(peerId: String): PeerStatus

    /**
     * Observe status changes for a specific peer.
     */
    fun observePeerStatus(peerId: String): Flow<PeerStatus>

    // ========== Direct Messaging ==========

    /**
     * Send a message directly to a peer.
     * Returns success if message was delivered or queued for store-and-forward.
     */
    suspend fun sendMessage(message: P2PMessage): Result<Unit>

    /**
     * Subscribe to incoming P2P messages.
     */
    fun subscribeToMessages(): Flow<P2PMessage>

    /**
     * Send a delivery receipt to confirm message was received.
     */
    suspend fun sendDeliveryReceipt(messageId: String, recipientId: String): Result<Unit>

    /**
     * Send a read receipt to confirm message was read.
     */
    suspend fun sendReadReceipt(messageId: String, recipientId: String): Result<Unit>

    // ========== Store-and-Forward (Offline Messages) ==========

    /**
     * Store a message for offline delivery.
     * Message will be delivered when recipient comes online.
     */
    suspend fun storeForwardMessage(message: P2PMessage): Result<Unit>

    /**
     * Retrieve stored messages for the current user.
     * Called when coming back online.
     */
    suspend fun retrieveStoredMessages(): Result<List<StoredMessage>>

    /**
     * Delete a stored message after successful delivery.
     */
    suspend fun deleteStoredMessage(messageId: String): Result<Unit>

    /**
     * Request mutual peers to store a message for offline recipient.
     */
    suspend fun requestStoreAndForward(
        recipientId: String,
        message: P2PMessage,
        storagePeers: Int = 3
    ): Result<List<String>> // Returns list of peer IDs that accepted storage

    // ========== Peer Connections ==========

    /**
     * Connect to a specific peer.
     */
    suspend fun connectToPeer(peerId: String): Result<PeerConnection>

    /**
     * Disconnect from a peer.
     */
    suspend fun disconnectFromPeer(peerId: String): Result<Unit>

    /**
     * Maintain connections (keep-alive, reconnect if needed).
     * Should be called periodically.
     */
    suspend fun maintainConnections(): Result<Unit>

    // ========== Network Events ==========

    /**
     * Observe all network events (connections, disconnections, messages, etc.)
     */
    fun observeNetworkEvents(): Flow<NetworkEvent>

    // ========== DHT Operations ==========

    /**
     * Publish local peer info to DHT.
     * Makes this peer discoverable by others.
     */
    suspend fun publishToDHT(key: String, value: String): Result<Unit>

    /**
     * Lookup a value in the DHT.
     * Used for peer discovery by phone hash or username.
     */
    suspend fun lookupInDHT(key: String): Result<String?>

    /**
     * Remove a value from the DHT.
     */
    suspend fun removeFromDHT(key: String): Result<Unit>

    // ========== Utility ==========

    /**
     * Calculate reliability score for a peer.
     * Based on message delivery success, latency, and uptime.
     */
    suspend fun calculatePeerReliability(peerId: String): Double

    /**
     * Find mutual peers between two users.
     * Used for store-and-forward routing.
     */
    suspend fun findMutualPeers(userId1: String, userId2: String): List<Peer>

    /**
     * Bootstrap DHT from public nodes.
     */
    suspend fun bootstrapDHT(bootstrapNodes: List<String>): Result<Unit>
}
