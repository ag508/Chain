package com.chain.app.domain.model

/**
 * Blockchain transaction for messages.
 */
data class MessageTransaction(
    val txHash: String,
    val from: String,
    val to: String,
    val encryptedPayload: String,
    val messageType: MessageType,
    val timestamp: Long,
    val blockNumber: Long,
    val gasUsed: Long,
    val signature: String,
    val nonce: String
)

/**
 * Block in the Chain blockchain.
 */
data class Block(
    val number: Long,
    val hash: String,
    val previousHash: String,
    val timestamp: Long,
    val transactions: List<MessageTransaction>,
    val merkleRoot: String,
    val nonce: String,
    val difficulty: Int
)

/**
 * Peer in the P2P network.
 */
data class Peer(
    val id: String,
    val address: String,
    val publicKey: String,
    val lastSeen: Long,
    val reliability: Double // 0.0 to 1.0
)

/**
 * Network status information.
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val connectedPeers: Int,
    val syncProgress: Double, // 0.0 to 1.0
    val blockHeight: Long,
    val lastBlockTime: Long
)

/**
 * Network event for P2P communication.
 */
sealed class NetworkEvent {
    data class PeerConnected(val peer: Peer) : NetworkEvent()
    data class PeerDisconnected(val peerId: String) : NetworkEvent()
    data class MessageReceived(val transaction: MessageTransaction) : NetworkEvent()
    data class BlockReceived(val block: Block) : NetworkEvent()
    data class SyncProgress(val progress: Double) : NetworkEvent()
}
