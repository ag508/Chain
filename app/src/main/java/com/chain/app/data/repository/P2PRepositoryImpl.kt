package com.chain.app.data.repository

import com.chain.app.data.p2p.dht.DHTManager
import com.chain.app.data.p2p.mdns.MDNSManager
import com.chain.app.data.p2p.storeforward.StoreForwardManager
import com.chain.app.data.p2p.webrtc.WebRTCDataChannelManager
import com.chain.app.domain.model.*
import com.chain.app.domain.repository.P2PRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of P2P repository.
 * Coordinates WebRTC data channels, DHT, mDNS, and store-and-forward for decentralized messaging.
 */
@Singleton
class P2PRepositoryImpl @Inject constructor(
    private val webrtcManager: WebRTCDataChannelManager,
    private val dhtManager: DHTManager,
    private val mdnsManager: MDNSManager,
    private val storeForwardManager: StoreForwardManager
) : P2PRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Local node information
    private lateinit var nodeInfo: NetworkInfo

    // Connected peers: peerId -> PeerConnection
    private val connectedPeers = ConcurrentHashMap<String, PeerConnection>()

    // Network status
    private val _networkStatus = MutableStateFlow(
        NetworkStatus(
            isConnected = false,
            connectedPeers = 0,
            availablePeers = 0,
            localDiscoveredPeers = 0,
            globalDiscoveredPeers = 0,
            activeConnections = 0,
            lastUpdate = System.currentTimeMillis()
        )
    )

    // Combined network events
    private val _networkEvents = MutableSharedFlow<NetworkEvent>(replay = 0, extraBufferCapacity = 64)

    // Incoming messages
    private val _incomingMessages = MutableSharedFlow<P2PMessage>(replay = 0, extraBufferCapacity = 64)

    init {
        // Collect WebRTC network events
        scope.launch {
            webrtcManager.networkEvents.collect { event ->
                _networkEvents.emit(event)
            }
        }

        // Collect WebRTC incoming data
        scope.launch {
            webrtcManager.incomingMessages.collect { dataChannelMsg ->
                // Deserialize P2P message from bytes
                try {
                    val p2pMessage = deserializeP2PMessage(dataChannelMsg.data)
                    _incomingMessages.emit(p2pMessage)
                    Timber.d("Received P2P message from ${dataChannelMsg.peerId}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to deserialize incoming message")
                }
            }
        }

        // Collect DHT discovered peers
        scope.launch {
            dhtManager.discoveredPeers.collect { peer ->
                updateNetworkStatus()
                _networkEvents.emit(NetworkEvent.PeerConnected(peer))
            }
        }

        // Collect mDNS discovered peers
        scope.launch {
            mdnsManager.discoveredPeers.collect { peer ->
                updateNetworkStatus()
                _networkEvents.emit(NetworkEvent.PeerConnected(peer))
            }
        }
    }

    // ========== Node Lifecycle ==========

    override suspend fun startNode(): Result<NetworkInfo> {
        return try {
            // Generate local peer ID
            val localPeerId = generatePeerId()
            val localPublicKey = generatePublicKey() // TODO: Use actual public key from encryption

            // Initialize WebRTC
            webrtcManager.initialize().getOrThrow()

            // Bootstrap DHT
            dhtManager.bootstrap().getOrThrow()

            // Start mDNS discovery
            mdnsManager.startDiscovery(localPeerId, localPublicKey).getOrThrow()

            // Create node info
            nodeInfo = NetworkInfo(
                localPeerId = localPeerId,
                localPublicKey = localPublicKey,
                listeningAddresses = listOf("0.0.0.0:0"), // WebRTC doesn't expose specific addresses
                isDHTEnabled = true,
                isMDNSEnabled = true,
                bootstrapNodes = listOf() // Using default IPFS nodes
            )

            // Publish to DHT
            dhtManager.publishPeer(localPeerId, "$localPeerId:$localPublicKey").getOrThrow()

            updateNetworkStatus()

            Timber.d("P2P node started: $localPeerId")
            Result.success(nodeInfo)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start P2P node")
            Result.failure(e)
        }
    }

    override suspend fun stopNode(): Result<Unit> {
        return try {
            mdnsManager.stopDiscovery().getOrThrow()
            dhtManager.shutdown()
            webrtcManager.shutdown()
            connectedPeers.clear()

            _networkStatus.value = _networkStatus.value.copy(
                isConnected = false,
                connectedPeers = 0,
                activeConnections = 0
            )

            Timber.d("P2P node stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop P2P node")
            Result.failure(e)
        }
    }

    override fun getNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()

    override suspend fun getNodeInfo(): NetworkInfo = nodeInfo

    // ========== Peer Discovery ==========

    override suspend fun discoverLocalPeers(): Flow<Peer> {
        return mdnsManager.discoveredPeers
    }

    override suspend fun discoverGlobalPeers(): Flow<Peer> {
        // Trigger DHT discovery
        scope.launch {
            dhtManager.discoverPeers()
        }
        return dhtManager.discoveredPeers
    }

    override suspend fun findPeer(peerId: String): Result<Peer> {
        return try {
            // Check local peers first
            val localPeers = mdnsManager.getActivePeers()
            localPeers.find { it.id == peerId }?.let {
                return Result.success(it)
            }

            // Query DHT
            val peerInfo = dhtManager.findPeer(peerId).getOrNull()
            if (peerInfo != null) {
                val parts = peerInfo.split(":")
                val peer = Peer(
                    id = parts[0],
                    address = parts.getOrNull(1) ?: "",
                    publicKey = parts.getOrNull(2) ?: "",
                    lastSeen = System.currentTimeMillis(),
                    reliability = 0.8,
                    isOnline = false
                )
                Result.success(peer)
            } else {
                Result.failure(IllegalStateException("Peer not found: $peerId"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find peer: $peerId")
            Result.failure(e)
        }
    }

    override fun getConnectedPeers(): Flow<List<PeerConnection>> {
        return flow {
            emit(connectedPeers.values.toList())
        }
    }

    override suspend fun getPeerStatus(peerId: String): PeerStatus {
        val connection = connectedPeers[peerId]
        return when {
            connection?.dataChannelOpen == true -> PeerStatus.ONLINE
            connection != null -> PeerStatus.CONNECTING
            else -> PeerStatus.OFFLINE
        }
    }

    override fun observePeerStatus(peerId: String): Flow<PeerStatus> {
        return _networkEvents.filterIsInstance<NetworkEvent.PeerStatusChanged>()
            .filter { it.peerId == peerId }
            .map { it.status }
    }

    // ========== Direct Messaging ==========

    override suspend fun sendMessage(message: P2PMessage): Result<Unit> {
        return try {
            val peerId = message.to

            // Check if peer is connected
            val peerConnection = connectedPeers[peerId]

            if (peerConnection?.dataChannelOpen == true) {
                // Send directly via WebRTC data channel
                val serialized = serializeP2PMessage(message)
                webrtcManager.sendData(peerId, serialized).getOrThrow()
                Timber.d("Message sent directly to $peerId")
            } else {
                // Peer offline or not connected - use store-and-forward
                Timber.d("Peer $peerId offline, using store-and-forward")
                storeForwardMessage(message).getOrThrow()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            Result.failure(e)
        }
    }

    override fun subscribeToMessages(): Flow<P2PMessage> = _incomingMessages.asSharedFlow()

    override suspend fun sendDeliveryReceipt(messageId: String, recipientId: String): Result<Unit> {
        // Create receipt message
        val receipt = P2PMessage(
            id = UUID.randomUUID().toString(),
            from = nodeInfo.localPeerId,
            to = recipientId,
            encryptedPayload = messageId.toByteArray(),
            timestamp = System.currentTimeMillis(),
            type = P2PMessageType.DELIVERY_RECEIPT,
            signature = ByteArray(0) // TODO: Sign with private key
        )
        return sendMessage(receipt)
    }

    override suspend fun sendReadReceipt(messageId: String, recipientId: String): Result<Unit> {
        val receipt = P2PMessage(
            id = UUID.randomUUID().toString(),
            from = nodeInfo.localPeerId,
            to = recipientId,
            encryptedPayload = messageId.toByteArray(),
            timestamp = System.currentTimeMillis(),
            type = P2PMessageType.READ_RECEIPT,
            signature = ByteArray(0) // TODO: Sign with private key
        )
        return sendMessage(receipt)
    }

    // ========== Store-and-Forward ==========

    override suspend fun storeForwardMessage(message: P2PMessage): Result<Unit> {
        return try {
            storeForwardManager.storeMessage(message.to, message).getOrThrow()
            Timber.d("Message stored for offline delivery to ${message.to}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to store message for offline delivery")
            Result.failure(e)
        }
    }

    override suspend fun retrieveStoredMessages(): Result<List<StoredMessage>> {
        return storeForwardManager.retrieveOwnMessages()
    }

    override suspend fun deleteStoredMessage(messageId: String): Result<Unit> {
        return storeForwardManager.markAsDelivered(messageId)
    }

    override suspend fun requestStoreAndForward(
        recipientId: String,
        message: P2PMessage,
        storagePeers: Int
    ): Result<List<String>> {
        return try {
            // Find mutual peers
            val mutualPeers = findMutualPeers(nodeInfo.localPeerId, recipientId)

            val storedOn = mutableListOf<String>()

            // Request storage from up to N random peers
            mutualPeers.shuffled().take(storagePeers).forEach { peer ->
                // Send store request to peer
                val storeRequest = P2PMessage(
                    id = UUID.randomUUID().toString(),
                    from = nodeInfo.localPeerId,
                    to = peer.id,
                    encryptedPayload = serializeP2PMessage(message),
                    timestamp = System.currentTimeMillis(),
                    type = P2PMessageType.STORE_FORWARD_REQUEST,
                    signature = ByteArray(0)
                )

                sendMessage(storeRequest).onSuccess {
                    storedOn.add(peer.id)
                }
            }

            Timber.d("Message stored on ${storedOn.size} peers for $recipientId")
            Result.success(storedOn)
        } catch (e: Exception) {
            Timber.e(e, "Failed to request store-and-forward")
            Result.failure(e)
        }
    }

    // ========== Peer Connections ==========

    override suspend fun connectToPeer(peerId: String): Result<PeerConnection> {
        return try {
            // Check if already connected
            connectedPeers[peerId]?.let {
                return Result.success(it)
            }

            // Create WebRTC connection
            webrtcManager.connectToPeer(peerId, isInitiator = true).getOrThrow()

            val connection = PeerConnection(
                peerId = peerId,
                status = PeerStatus.ONLINE,
                connectedAt = System.currentTimeMillis(),
                lastActivity = System.currentTimeMillis(),
                dataChannelOpen = true
            )

            connectedPeers[peerId] = connection
            updateNetworkStatus()

            Timber.d("Connected to peer: $peerId")
            Result.success(connection)
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect to peer: $peerId")
            Result.failure(e)
        }
    }

    override suspend fun disconnectFromPeer(peerId: String): Result<Unit> {
        return try {
            webrtcManager.disconnectFromPeer(peerId)
            connectedPeers.remove(peerId)
            updateNetworkStatus()

            Timber.d("Disconnected from peer: $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to disconnect from peer: $peerId")
            Result.failure(e)
        }
    }

    override suspend fun maintainConnections(): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val staleThreshold = 300000L // 5 minutes

            // Check for stale connections
            connectedPeers.values.forEach { connection ->
                if (now - connection.lastActivity > staleThreshold) {
                    Timber.d("Connection to ${connection.peerId} is stale, reconnecting")
                    disconnectFromPeer(connection.peerId)
                    connectToPeer(connection.peerId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to maintain connections")
            Result.failure(e)
        }
    }

    // ========== Network Events ==========

    override fun observeNetworkEvents(): Flow<NetworkEvent> = _networkEvents.asSharedFlow()

    // ========== DHT Operations ==========

    override suspend fun publishToDHT(key: String, value: String): Result<Unit> {
        return dhtManager.publishPeer(key, value)
    }

    override suspend fun lookupInDHT(key: String): Result<String?> {
        return dhtManager.findPeer(key)
    }

    override suspend fun removeFromDHT(key: String): Result<Unit> {
        return dhtManager.removePeer(key)
    }

    // ========== Utility ==========

    override suspend fun calculatePeerReliability(peerId: String): Double {
        // TODO: Implement reliability scoring based on message delivery success, latency, uptime
        return 0.8
    }

    override suspend fun findMutualPeers(userId1: String, userId2: String): List<Peer> {
        // TODO: Implement actual mutual peer discovery
        // For now, return local peers
        return mdnsManager.getActivePeers()
    }

    override suspend fun bootstrapDHT(bootstrapNodes: List<String>): Result<Unit> {
        return dhtManager.bootstrap(bootstrapNodes)
    }

    // ========== Private Methods ==========

    private fun updateNetworkStatus() {
        val localPeers = mdnsManager.getActivePeers().size
        val globalPeers = 0 // TODO: Get from DHT

        _networkStatus.value = NetworkStatus(
            isConnected = true,
            connectedPeers = connectedPeers.size,
            availablePeers = localPeers + globalPeers,
            localDiscoveredPeers = localPeers,
            globalDiscoveredPeers = globalPeers,
            activeConnections = connectedPeers.values.count { it.dataChannelOpen },
            lastUpdate = System.currentTimeMillis()
        )
    }

    private fun generatePeerId(): String {
        return "peer-${UUID.randomUUID()}"
    }

    private fun generatePublicKey(): String {
        // TODO: Get actual public key from encryption service
        return "pubkey-${UUID.randomUUID()}"
    }

    private fun serializeP2PMessage(message: P2PMessage): ByteArray {
        // TODO: Implement proper serialization (Protobuf, JSON, etc.)
        // For now, simple concatenation
        val data = "${message.id}|${message.from}|${message.to}|${message.timestamp}|${message.type}|${message.encryptedPayload.size}"
        return data.toByteArray() + message.encryptedPayload + message.signature
    }

    private fun deserializeP2PMessage(data: ByteArray): P2PMessage {
        // TODO: Implement proper deserialization
        // This is a simplified placeholder
        return P2PMessage(
            id = UUID.randomUUID().toString(),
            from = "unknown",
            to = nodeInfo.localPeerId,
            encryptedPayload = data,
            timestamp = System.currentTimeMillis(),
            type = P2PMessageType.CHAT_MESSAGE,
            signature = ByteArray(0)
        )
    }
}
