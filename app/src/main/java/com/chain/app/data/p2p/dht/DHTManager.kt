package com.chain.app.data.p2p.dht

import com.chain.app.domain.model.Peer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Distributed Hash Table for global peer discovery using Kademlia algorithm.
 *
 * Implements core Kademlia DHT operations:
 * - K-bucket routing table (256 buckets for 256-bit SHA-256 node IDs)
 * - FIND_NODE lookups for peer discovery
 * - STORE/FIND_VALUE for peer information storage
 * - Bootstrap with seed nodes
 */
@Singleton
class DHTManager @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Local node ID (generated from user ID)
    private var localNode: KademliaNode? = null

    // K-buckets for routing table (256 buckets for SHA-256)
    private val kBuckets = Array(256) { KBucket(k = 20) }

    // Stored peer information: key -> peer data
    private val storedPeers = ConcurrentHashMap<String, String>()

    // Known peers: hash -> peer info
    private val knownPeers = ConcurrentHashMap<String, Peer>()

    // Discovered peers channel
    private val _discoveredPeers = Channel<Peer>(Channel.BUFFERED)
    val discoveredPeers: Flow<Peer> = _discoveredPeers.receiveAsFlow()

    // DHT status
    private val _status = MutableStateFlow(DHTStatus(false, 0, 0))
    val status: StateFlow<DHTStatus> = _status.asStateFlow()

    // Alpha parameter - number of parallel lookups
    private val alpha = 3

    /**
     * Initialize DHT with local node information.
     */
    fun initialize(userId: String, address: String, port: Int) {
        val nodeId = KademliaNode.generateNodeId(userId)
        localNode = KademliaNode(nodeId, address, port)
        Timber.d("DHT initialized with node ID: ${nodeId.joinToString("") { "%02x".format(it) }}")
    }

    /**
     * Bootstrap DHT with seed nodes.
     */
    suspend fun bootstrap(bootstrapNodes: List<String>): Result<Unit> {
        return try {
            Timber.d("DHT bootstrap starting with ${bootstrapNodes.size} seed nodes")

            if (localNode == null) {
                return Result.failure(Exception("DHT not initialized"))
            }

            // Parse and add bootstrap nodes
            bootstrapNodes.forEach { nodeString ->
                try {
                    // Format: nodeId@address:port
                    val parts = nodeString.split("@")
                    if (parts.size == 2) {
                        val nodeId = parts[0].chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                        val addressParts = parts[1].split(":")
                        val address = addressParts[0]
                        val port = addressParts.getOrNull(1)?.toIntOrNull() ?: 8080

                        val node = KademliaNode(nodeId, address, port)
                        addNode(node)
                        Timber.d("Added bootstrap node: $address:$port")
                    }
                } catch (e: Exception) {
                    Timber.w("Failed to parse bootstrap node: $nodeString - ${e.message}")
                }
            }

            // Perform node lookup on our own ID to populate routing table
            if (bootstrapNodes.isNotEmpty()) {
                findNode(localNode!!.id)
            }

            updateStatus()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "DHT bootstrap failed")
            Result.failure(e)
        }
    }

    /**
     * Add a node to the appropriate k-bucket.
     */
    private fun addNode(node: KademliaNode) {
        if (localNode == null || node.id.contentEquals(localNode!!.id)) {
            return // Don't add self
        }

        val distance = KademliaNode.xorDistance(localNode!!.id, node.id)
        val bucketIndex = KademliaNode.getBucketIndex(distance)

        if (bucketIndex in kBuckets.indices) {
            kBuckets[bucketIndex].addNode(node)
            Timber.d("Added node to bucket $bucketIndex")
        }
    }

    /**
     * Find closest nodes to a target ID (FIND_NODE operation).
     */
    private fun findNode(targetId: ByteArray): List<KademliaNode> {
        val closestNodes = mutableListOf<KademliaNode>()

        // Collect nodes from all buckets
        kBuckets.forEach { bucket ->
            closestNodes.addAll(bucket.getNodes())
        }

        // Sort by distance to target and take closest K nodes
        return closestNodes.sortedBy { node ->
            val distance = KademliaNode.xorDistance(targetId, node.id)
            distance.joinToString("") { "%02x".format(it) }
        }.take(20)
    }

    /**
     * Publish peer information to DHT (STORE operation).
     */
    suspend fun publishPeer(key: String, peerInfo: String): Result<Unit> {
        return try {
            if (localNode == null) {
                return Result.failure(Exception("DHT not initialized"))
            }

            Timber.d("DHT publishing peer: $key")

            // Store locally
            storedPeers[key] = peerInfo

            // In full implementation, would also store on K closest nodes
            // For now, just store locally

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to publish peer to DHT")
            Result.failure(e)
        }
    }

    /**
     * Find peer in DHT by key (FIND_VALUE operation).
     */
    suspend fun findPeer(key: String): Result<String?> {
        return try {
            if (localNode == null) {
                return Result.failure(Exception("DHT not initialized"))
            }

            Timber.d("DHT finding peer: $key")

            // Check local storage first
            val localValue = storedPeers[key]
            if (localValue != null) {
                Timber.d("Found peer locally: $key")
                return Result.success(localValue)
            }

            // In full implementation, would query K closest nodes
            // For now, return null if not found locally
            Timber.d("Peer not found in DHT: $key")
            Result.success(null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to find peer in DHT")
            Result.failure(e)
        }
    }

    /**
     * Remove peer from DHT.
     */
    suspend fun removePeer(key: String): Result<Unit> {
        return try {
            Timber.d("DHT removing peer: $key")
            storedPeers.remove(key)
            knownPeers.remove(key)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove peer from DHT")
            Result.failure(e)
        }
    }

    /**
     * Discover peers in the DHT network.
     */
    suspend fun discoverPeers(): Result<List<Peer>> {
        return try {
            Timber.d("DHT discovering peers")

            val discoveredPeers = mutableListOf<Peer>()

            // Collect all nodes from routing table
            kBuckets.forEach { bucket ->
                bucket.getNodes().forEach { node ->
                    // Convert KademliaNode to Peer
                    val peer = Peer(
                        id = node.id.joinToString("") { "%02x".format(it) },
                        address = "${node.address}:${node.port}",
                        publicKey = "", // Will be exchanged during handshake
                        lastSeen = node.lastSeen,
                        reliability = 1.0, // Default to full reliability for DHT peers
                        isOnline = true
                    )
                    discoveredPeers.add(peer)
                    knownPeers[peer.id] = peer

                    // Emit to discovered peers channel
                    scope.launch {
                        _discoveredPeers.send(peer)
                    }
                }
            }

            Timber.d("Discovered ${discoveredPeers.size} peers from DHT")
            updateStatus()
            Result.success(discoveredPeers)
        } catch (e: Exception) {
            Timber.e(e, "Failed to discover peers")
            Result.failure(e)
        }
    }

    /**
     * Get K closest nodes to a target.
     */
    fun getClosestNodes(targetId: ByteArray, k: Int = 20): List<KademliaNode> {
        return findNode(targetId).take(k)
    }

    /**
     * Update DHT status.
     */
    private fun updateStatus() {
        val totalNodes = kBuckets.sumOf { it.size() }
        val isConnected = totalNodes > 0
        _status.value = DHTStatus(
            isConnected = isConnected,
            knownPeers = totalNodes,
            bucketSize = kBuckets.count { it.size() > 0 }
        )
    }

    /**
     * Shutdown DHT manager.
     */
    suspend fun shutdown(): Result<Unit> {
        return try {
            Timber.d("DHT shutdown")
            kBuckets.forEach { it.clear() }
            storedPeers.clear()
            knownPeers.clear()
            localNode = null
            _status.value = DHTStatus(isConnected = false, knownPeers = 0, bucketSize = 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to shutdown DHT")
            Result.failure(e)
        }
    }
}

/**
 * DHT connection status.
 */
data class DHTStatus(
    val isConnected: Boolean,
    val knownPeers: Int,
    val bucketSize: Int
)
