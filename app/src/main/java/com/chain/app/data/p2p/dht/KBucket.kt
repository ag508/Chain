package com.chain.app.data.p2p.dht

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * K-bucket for storing nodes in Kademlia DHT.
 * Each bucket stores up to K nodes with similar distances to the local node.
 */
class KBucket(
    private val k: Int = 20 // Standard Kademlia bucket size
) {
    private val nodes = ConcurrentLinkedQueue<KademliaNode>()
    private val seenNodes = mutableSetOf<ByteArray>()

    /**
     * Add or update a node in the bucket.
     * Implements the Kademlia bucket update logic:
     * - If node exists, move to tail (most recently seen)
     * - If bucket not full, add node
     * - If bucket full, try to ping least recently seen node
     */
    fun addNode(node: KademliaNode): Boolean {
        synchronized(this) {
            // Check if node already exists
            val existing = nodes.find { it.id.contentEquals(node.id) }
            if (existing != null) {
                // Move to end (most recently seen)
                nodes.remove(existing)
                existing.lastSeen = System.currentTimeMillis()
                nodes.add(existing)
                return true
            }

            // Add if space available
            if (nodes.size < k) {
                nodes.add(node)
                seenNodes.add(node.id)
                return true
            }

            // Bucket full - in production, should ping least recently seen node
            // For now, don't add new nodes when bucket is full
            return false
        }
    }

    /**
     * Remove a node from the bucket.
     */
    fun removeNode(nodeId: ByteArray): Boolean {
        synchronized(this) {
            val removed = nodes.removeIf { it.id.contentEquals(nodeId) }
            if (removed) {
                seenNodes.removeIf { it.contentEquals(nodeId) }
            }
            return removed
        }
    }

    /**
     * Get all nodes in the bucket.
     */
    fun getNodes(): List<KademliaNode> {
        return nodes.toList()
    }

    /**
     * Check if bucket contains a node.
     */
    fun contains(nodeId: ByteArray): Boolean {
        return nodes.any { it.id.contentEquals(nodeId) }
    }

    /**
     * Get bucket size.
     */
    fun size(): Int {
        return nodes.size
    }

    /**
     * Check if bucket is full.
     */
    fun isFull(): Boolean {
        return nodes.size >= k
    }

    /**
     * Get the least recently seen node.
     */
    fun getLeastRecentlySeen(): KademliaNode? {
        return nodes.firstOrNull()
    }

    /**
     * Clear all nodes from bucket.
     */
    fun clear() {
        synchronized(this) {
            nodes.clear()
            seenNodes.clear()
        }
    }
}
