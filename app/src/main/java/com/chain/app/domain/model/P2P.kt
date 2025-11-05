package com.chain.app.domain.model

import java.util.Date

/**
 * Peer in the P2P network.
 */
data class Peer(
    val id: String,
    val address: String,
    val publicKey: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val lastSeen: Long,
    val reliability: Double, // 0.0 to 1.0
    val isOnline: Boolean = false,
    val latency: Long = 0 // milliseconds
)

/**
 * Status of a peer in the network.
 */
enum class PeerStatus {
    ONLINE,
    OFFLINE,
    CONNECTING,
    DISCONNECTING,
    AWAY
}

/**
 * Connection information for a peer.
 */
data class PeerConnection(
    val peerId: String,
    val status: PeerStatus,
    val connectedAt: Long,
    val lastActivity: Long,
    val dataChannelOpen: Boolean = false
)

/**
 * Network status information.
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val connectedPeers: Int,
    val availablePeers: Int,
    val localDiscoveredPeers: Int, // mDNS discovered
    val globalDiscoveredPeers: Int, // DHT discovered
    val activeConnections: Int,
    val lastUpdate: Long
)

/**
 * Information about the local network.
 */
data class NetworkInfo(
    val localPeerId: String,
    val localPublicKey: String,
    val listeningAddresses: List<String>,
    val isDHTEnabled: Boolean,
    val isMDNSEnabled: Boolean,
    val bootstrapNodes: List<String>
)

/**
 * Network event for P2P communication.
 */
sealed class NetworkEvent {
    data class PeerConnected(val peer: Peer) : NetworkEvent()
    data class PeerDisconnected(val peerId: String, val reason: String?) : NetworkEvent()
    data class MessageReceived(val peerId: String, val data: ByteArray) : NetworkEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MessageReceived

            if (peerId != other.peerId) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = peerId.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
    data class PeerStatusChanged(val peerId: String, val status: PeerStatus) : NetworkEvent()
    data class DiscoveryProgress(val localPeers: Int, val globalPeers: Int) : NetworkEvent()
    data class ConnectionError(val peerId: String, val error: String) : NetworkEvent()
}

/**
 * P2P message envelope for sending/receiving data.
 */
data class P2PMessage(
    val id: String,
    val from: String,
    val to: String,
    val encryptedPayload: ByteArray,
    val timestamp: Long,
    val type: P2PMessageType,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as P2PMessage

        if (id != other.id) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (timestamp != other.timestamp) return false
        if (type != other.type) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}

/**
 * Type of P2P message.
 */
enum class P2PMessageType {
    CHAT_MESSAGE,
    DELIVERY_RECEIPT,
    READ_RECEIPT,
    TYPING_INDICATOR,
    PRESENCE_UPDATE,
    CALL_SIGNAL,
    FILE_OFFER,
    STORE_FORWARD_REQUEST,
    STORE_FORWARD_DELIVERY
}

/**
 * Store-and-forward message for offline delivery.
 */
data class StoredMessage(
    val id: String,
    val recipientId: String,
    val encryptedMessage: P2PMessage,
    val storedAt: Long,
    val expiresAt: Long,
    val attempts: Int = 0,
    val maxAttempts: Int = 5
)
