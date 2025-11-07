package com.chain.app.data.p2p

import com.chain.app.domain.model.Peer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks and scores peer reliability based on their behavior.
 * Higher scores indicate more reliable peers for routing and messaging.
 */
@Singleton
class PeerReliabilityScorer @Inject constructor() {

    // Peer scores: peerId -> PeerScore
    private val peerScores = ConcurrentHashMap<String, PeerScore>()

    // Top reliable peers (cached for performance)
    private val _reliablePeers = MutableStateFlow<List<ScoredPeer>>(emptyList())
    val reliablePeers: StateFlow<List<ScoredPeer>> = _reliablePeers.asStateFlow()

    // Scoring weights
    private val UPTIME_WEIGHT = 0.3
    private val MESSAGE_DELIVERY_WEIGHT = 0.3
    private val RESPONSE_TIME_WEIGHT = 0.2
    private val ROUTING_SUCCESS_WEIGHT = 0.2

    /**
     * Record successful message delivery to/through a peer.
     */
    fun recordMessageDelivery(peerId: String, success: Boolean, responseTimeMs: Long = 0) {
        val score = peerScores.getOrPut(peerId) { PeerScore(peerId) }

        synchronized(score) {
            score.totalMessages++
            if (success) {
                score.successfulMessages++

                // Update average response time
                if (responseTimeMs > 0) {
                    val totalTime = score.averageResponseTimeMs * (score.successfulMessages - 1)
                    score.averageResponseTimeMs = (totalTime + responseTimeMs) / score.successfulMessages
                }
            } else {
                score.failedMessages++
            }

            score.lastUpdated = System.currentTimeMillis()
        }

        updateReliablePeers()
        Timber.d("Updated peer score for $peerId: ${calculateScore(peerId)}")
    }

    /**
     * Record successful routing through a peer.
     */
    fun recordRoutingSuccess(peerId: String, success: Boolean) {
        val score = peerScores.getOrPut(peerId) { PeerScore(peerId) }

        synchronized(score) {
            score.totalRoutingAttempts++
            if (success) {
                score.successfulRoutings++
            }
            score.lastUpdated = System.currentTimeMillis()
        }

        updateReliablePeers()
    }

    /**
     * Record peer uptime/downtime.
     */
    fun recordUptime(peerId: String, isOnline: Boolean) {
        val score = peerScores.getOrPut(peerId) { PeerScore(peerId) }

        synchronized(score) {
            val now = System.currentTimeMillis()

            if (isOnline) {
                if (score.lastSeen == 0L) {
                    score.firstSeen = now
                }
                score.lastSeen = now

                // Calculate uptime
                val totalTime = now - score.firstSeen
                if (totalTime > 0) {
                    score.uptimePercentage = ((now - score.firstSeen).toDouble() / totalTime * 100).toInt()
                }
            } else {
                // Peer went offline
                score.totalDowntime++
            }

            score.lastUpdated = now
        }

        updateReliablePeers()
    }

    /**
     * Record peer misbehavior (spam, invalid messages, etc.).
     */
    fun recordMisbehavior(peerId: String, severity: MisbehaviorSeverity) {
        val score = peerScores.getOrPut(peerId) { PeerScore(peerId) }

        synchronized(score) {
            when (severity) {
                MisbehaviorSeverity.LOW -> score.minorInfractions++
                MisbehaviorSeverity.MEDIUM -> score.moderateInfractions++
                MisbehaviorSeverity.HIGH -> score.severeInfractions++
            }
            score.lastUpdated = System.currentTimeMillis()
        }

        updateReliablePeers()
        Timber.w("Recorded $severity misbehavior for peer $peerId")
    }

    /**
     * Calculate overall reliability score (0-100).
     */
    fun calculateScore(peerId: String): Double {
        val score = peerScores[peerId] ?: return 0.0

        synchronized(score) {
            // 1. Message delivery score
            val deliveryScore = if (score.totalMessages > 0) {
                (score.successfulMessages.toDouble() / score.totalMessages) * 100
            } else {
                50.0 // Neutral score for new peers
            }

            // 2. Uptime score
            val uptimeScore = score.uptimePercentage.toDouble()

            // 3. Response time score (lower is better, normalize to 0-100)
            val responseScore = if (score.averageResponseTimeMs > 0) {
                maxOf(0.0, 100.0 - (score.averageResponseTimeMs / 100.0))
            } else {
                50.0
            }

            // 4. Routing success score
            val routingScore = if (score.totalRoutingAttempts > 0) {
                (score.successfulRoutings.toDouble() / score.totalRoutingAttempts) * 100
            } else {
                50.0
            }

            // Calculate weighted score
            var totalScore = (deliveryScore * MESSAGE_DELIVERY_WEIGHT) +
                    (uptimeScore * UPTIME_WEIGHT) +
                    (responseScore * RESPONSE_TIME_WEIGHT) +
                    (routingScore * ROUTING_SUCCESS_WEIGHT)

            // Apply penalties for misbehavior
            totalScore -= (score.minorInfractions * 1.0)
            totalScore -= (score.moderateInfractions * 5.0)
            totalScore -= (score.severeInfractions * 20.0)

            // Ensure score is between 0 and 100
            return totalScore.coerceIn(0.0, 100.0)
        }
    }

    /**
     * Get peer score details.
     */
    fun getPeerScore(peerId: String): PeerScore? {
        return peerScores[peerId]
    }

    /**
     * Get top N most reliable peers.
     */
    fun getTopPeers(count: Int = 10): List<ScoredPeer> {
        return peerScores.entries
            .map { ScoredPeer(it.key, calculateScore(it.key)) }
            .sortedByDescending { it.score }
            .take(count)
    }

    /**
     * Check if a peer is trustworthy (score > 70).
     */
    fun isTrustworthy(peerId: String): Boolean {
        return calculateScore(peerId) > 70.0
    }

    /**
     * Check if a peer should be banned (score < 20).
     */
    fun shouldBan(peerId: String): Boolean {
        val score = calculateScore(peerId)
        val peerScore = peerScores[peerId]

        return score < 20.0 || (peerScore?.severeInfractions ?: 0) >= 3
    }

    /**
     * Update the list of reliable peers.
     */
    private fun updateReliablePeers() {
        _reliablePeers.value = getTopPeers(20)
    }

    /**
     * Reset score for a peer.
     */
    fun resetScore(peerId: String) {
        peerScores.remove(peerId)
        updateReliablePeers()
        Timber.d("Reset score for peer: $peerId")
    }

    /**
     * Clear all scores.
     */
    fun clearAllScores() {
        peerScores.clear()
        _reliablePeers.value = emptyList()
        Timber.d("Cleared all peer scores")
    }
}

/**
 * Detailed peer score tracking.
 */
data class PeerScore(
    val peerId: String,
    var totalMessages: Long = 0,
    var successfulMessages: Long = 0,
    var failedMessages: Long = 0,
    var averageResponseTimeMs: Long = 0,
    var totalRoutingAttempts: Long = 0,
    var successfulRoutings: Long = 0,
    var uptimePercentage: Int = 100,
    var firstSeen: Long = System.currentTimeMillis(),
    var lastSeen: Long = System.currentTimeMillis(),
    var totalDowntime: Long = 0,
    var minorInfractions: Int = 0,
    var moderateInfractions: Int = 0,
    var severeInfractions: Int = 0,
    var lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Peer with calculated reliability score.
 */
data class ScoredPeer(
    val peerId: String,
    val score: Double
)

/**
 * Severity levels for peer misbehavior.
 */
enum class MisbehaviorSeverity {
    LOW,    // Minor issues (slow response, occasional timeout)
    MEDIUM, // Moderate issues (frequent timeouts, invalid data)
    HIGH    // Severe issues (spam, malicious behavior, protocol violations)
}
