package com.chain.app.data.p2p.dht

import java.security.MessageDigest
import kotlin.math.min

/**
 * Represents a node in the Kademlia DHT network.
 */
data class KademliaNode(
    val id: ByteArray,
    val address: String,
    val port: Int,
    var lastSeen: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KademliaNode) return false
        return id.contentEquals(other.id)
    }

    override fun hashCode(): Int {
        return id.contentHashCode()
    }

    companion object {
        /**
         * Generate node ID from a string identifier (e.g., user ID).
         */
        fun generateNodeId(identifier: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(identifier.toByteArray())
        }

        /**
         * Calculate XOR distance between two node IDs.
         */
        fun xorDistance(id1: ByteArray, id2: ByteArray): ByteArray {
            val result = ByteArray(min(id1.size, id2.size))
            for (i in result.indices) {
                result[i] = (id1[i].toInt() xor id2[i].toInt()).toByte()
            }
            return result
        }

        /**
         * Compare two distances (returns -1, 0, or 1).
         */
        fun compareDistance(distance1: ByteArray, distance2: ByteArray): Int {
            for (i in 0 until min(distance1.size, distance2.size)) {
                val d1 = distance1[i].toInt() and 0xFF
                val d2 = distance2[i].toInt() and 0xFF
                if (d1 < d2) return -1
                if (d1 > d2) return 1
            }
            return 0
        }

        /**
         * Get the bucket index for a given distance.
         * Returns the position of the most significant bit.
         */
        fun getBucketIndex(distance: ByteArray): Int {
            for (i in distance.indices) {
                val byte = distance[i].toInt() and 0xFF
                if (byte != 0) {
                    // Find most significant bit position
                    var msb = 7
                    var mask = 0x80
                    while (mask > 0) {
                        if (byte and mask != 0) {
                            return i * 8 + msb
                        }
                        mask = mask shr 1
                        msb--
                    }
                }
            }
            return 0
        }
    }
}
