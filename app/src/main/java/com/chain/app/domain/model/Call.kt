package com.chain.app.domain.model

import java.util.Date

/**
 * Call domain model representing a voice or video call.
 */
data class Call(
    val id: String,
    val chatId: String,
    val initiator: String,
    val participants: List<String>,
    val type: CallType,
    val status: CallStatus,
    val startTime: Date,
    val endTime: Date? = null,
    val duration: Long? = null // Duration in milliseconds
)

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    INITIATING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
    REJECTED,
    MISSED,
    FAILED
}

/**
 * Call session for active calls.
 */
data class CallSession(
    val call: Call,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val connectionQuality: ConnectionQuality = ConnectionQuality.GOOD
)

enum class ConnectionQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}
