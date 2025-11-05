package com.chain.app.data.p2p.signaling

import com.chain.app.data.p2p.dht.DHTManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebRTC signaling service using DHT for peer discovery and connection establishment.
 * Handles SDP offer/answer exchange and ICE candidate sharing.
 */
@Singleton
class WebRTCSignalingService @Inject constructor(
    private val dhtManager: DHTManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Incoming signaling messages
    private val _signalingMessages = Channel<SignalingMessage>(Channel.BUFFERED)
    val signalingMessages: Flow<SignalingMessage> = _signalingMessages.receiveAsFlow()

    /**
     * Send SDP offer to a peer via DHT.
     */
    suspend fun sendOffer(peerId: String, offer: SessionDescription): Result<Unit> {
        return try {
            val signal = SignalingMessage(
                from = "", // Will be set by caller
                to = peerId,
                type = SignalingType.OFFER,
                sdp = offer.description,
                candidate = null
            )

            val serialized = json.encodeToString(signal)
            val key = "webrtc_signal_$peerId"

            dhtManager.publishPeer(key, serialized).getOrThrow()

            Timber.d("Sent offer to peer: $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send offer to peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Send SDP answer to a peer via DHT.
     */
    suspend fun sendAnswer(peerId: String, answer: SessionDescription): Result<Unit> {
        return try {
            val signal = SignalingMessage(
                from = "", // Will be set by caller
                to = peerId,
                type = SignalingType.ANSWER,
                sdp = answer.description,
                candidate = null
            )

            val serialized = json.encodeToString(signal)
            val key = "webrtc_signal_$peerId"

            dhtManager.publishPeer(key, serialized).getOrThrow()

            Timber.d("Sent answer to peer: $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send answer to peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Send ICE candidate to a peer via DHT.
     */
    suspend fun sendIceCandidate(peerId: String, candidate: IceCandidate): Result<Unit> {
        return try {
            val candidateData = SerializableIceCandidate(
                sdp = candidate.sdp,
                sdpMid = candidate.sdpMid,
                sdpMLineIndex = candidate.sdpMLineIndex
            )

            val signal = SignalingMessage(
                from = "", // Will be set by caller
                to = peerId,
                type = SignalingType.ICE_CANDIDATE,
                sdp = null,
                candidate = candidateData
            )

            val serialized = json.encodeToString(signal)
            val key = "webrtc_ice_$peerId"

            dhtManager.publishPeer(key, serialized).getOrThrow()

            Timber.d("Sent ICE candidate to peer: $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send ICE candidate to peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Poll for signaling messages from DHT.
     * In production, you'd want a push-based approach or more efficient polling.
     */
    suspend fun pollSignalingMessages(localPeerId: String): Result<Unit> {
        return try {
            val key = "webrtc_signal_$localPeerId"

            // Query DHT for signaling messages
            val signalData = dhtManager.findPeer(key).getOrNull()

            if (signalData != null) {
                val signal = json.decodeFromString<SignalingMessage>(signalData)

                scope.launch {
                    _signalingMessages.send(signal)
                }

                Timber.d("Received signaling message: ${signal.type} from ${signal.from}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to poll signaling messages")
            Result.failure(e)
        }
    }

    /**
     * Poll for ICE candidates from DHT.
     */
    suspend fun pollIceCandidates(localPeerId: String): Result<List<IceCandidate>> {
        return try {
            val key = "webrtc_ice_$localPeerId"

            val candidateData = dhtManager.findPeer(key).getOrNull()

            if (candidateData != null) {
                val signal = json.decodeFromString<SignalingMessage>(candidateData)

                if (signal.candidate != null) {
                    val iceCandidate = IceCandidate(
                        signal.candidate.sdpMid,
                        signal.candidate.sdpMLineIndex,
                        signal.candidate.sdp
                    )

                    Timber.d("Received ICE candidate from ${signal.from}")
                    return Result.success(listOf(iceCandidate))
                }
            }

            Result.success(emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Failed to poll ICE candidates")
            Result.failure(e)
        }
    }

    /**
     * Start polling for signaling messages.
     * This should be called when the P2P node starts.
     */
    fun startPolling(localPeerId: String) {
        scope.launch {
            while (true) {
                pollSignalingMessages(localPeerId)
                pollIceCandidates(localPeerId)

                // Poll every 2 seconds (adjust as needed)
                kotlinx.coroutines.delay(2000)
            }
        }
    }
}

/**
 * Signaling message types.
 */
enum class SignalingType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE
}

/**
 * Signaling message structure.
 */
@Serializable
data class SignalingMessage(
    val from: String,
    val to: String,
    val type: SignalingType,
    val sdp: String?,
    val candidate: SerializableIceCandidate?
)

/**
 * Serializable ICE candidate.
 */
@Serializable
data class SerializableIceCandidate(
    val sdp: String,
    val sdpMid: String,
    val sdpMLineIndex: Int
)
