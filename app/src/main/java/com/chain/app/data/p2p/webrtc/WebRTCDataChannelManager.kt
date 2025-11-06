package com.chain.app.data.p2p.webrtc

import android.content.Context
import com.chain.app.domain.model.NetworkEvent
import com.chain.app.domain.model.P2PMessage
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
import org.webrtc.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WebRTC data channels for P2P messaging.
 * Handles peer connections, data channel creation, and message transmission.
 */
@Singleton
class WebRTCDataChannelManager @Inject constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Peer connection factory
    private lateinit var peerConnectionFactory: PeerConnectionFactory

    // Active peer connections: peerId -> PeerConnection
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()

    // Active data channels: peerId -> DataChannel
    private val dataChannels = ConcurrentHashMap<String, DataChannel>()

    // Incoming messages channel
    private val _incomingMessages = Channel<DataChannelMessage>(Channel.BUFFERED)
    val incomingMessages: Flow<DataChannelMessage> = _incomingMessages.receiveAsFlow()

    // Network events
    private val _networkEvents = Channel<NetworkEvent>(Channel.BUFFERED)
    val networkEvents: Flow<NetworkEvent> = _networkEvents.receiveAsFlow()

    // Connection states
    private val _connectionStates = MutableStateFlow<Map<String, PeerConnection.PeerConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, PeerConnection.PeerConnectionState>> = _connectionStates.asStateFlow()

    // STUN/TURN servers
    private val iceServers = listOf(
        // Public STUN servers
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        // Add TURN servers here if needed for production
        // PeerConnection.IceServer.builder("turn:your-turn-server:3478")
        //     .setUsername("username")
        //     .setPassword("password")
        //     .createIceServer()
    )

    private var isInitialized = false

    /**
     * Initialize WebRTC.
     */
    fun initialize(): Result<Unit> {
        return try {
            if (isInitialized) {
                return Result.success(Unit)
            }

            // Initialize PeerConnectionFactory
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            val encoderFactory = DefaultVideoEncoderFactory(
                EglBase.create().eglBaseContext,
                true,
                true
            )
            val decoderFactory = DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory()

            isInitialized = true
            Timber.d("WebRTC initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize WebRTC")
            Result.failure(e)
        }
    }

    /**
     * Create a peer connection and data channel to a peer.
     */
    suspend fun connectToPeer(peerId: String, isInitiator: Boolean): Result<DataChannel> {
        return try {
            if (!isInitialized) {
                initialize().getOrThrow()
            }

            // Check if already connected
            dataChannels[peerId]?.let {
                if (it.state() == DataChannel.State.OPEN) {
                    return Result.success(it)
                }
            }

            // Create peer connection
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }

            val peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfig,
                PeerConnectionObserver(peerId)
            ) ?: throw IllegalStateException("Failed to create peer connection")

            peerConnections[peerId] = peerConnection

            // Create or wait for data channel
            val dataChannel = if (isInitiator) {
                // Initiator creates the data channel
                val dataChannelInit = DataChannel.Init().apply {
                    ordered = true
                    negotiated = false
                }
                peerConnection.createDataChannel("messages", dataChannelInit)?.also {
                    it.registerObserver(DataChannelObserver(peerId))
                    dataChannels[peerId] = it
                } ?: throw IllegalStateException("Failed to create data channel")
            } else {
                // Responder waits for data channel (will be set in observer)
                dataChannels[peerId] ?: throw IllegalStateException("Data channel not created by remote peer")
            }

            Timber.d("Connected to peer: $peerId")
            scope.launch {
                _networkEvents.send(NetworkEvent.PeerConnected(
                    com.chain.app.domain.model.Peer(
                        id = peerId,
                        address = "",
                        publicKey = "",
                        lastSeen = System.currentTimeMillis(),
                        reliability = 1.0,
                        isOnline = true
                    )
                ))
            }

            Result.success(dataChannel)
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect to peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Send data to a peer via data channel.
     */
    suspend fun sendData(peerId: String, data: ByteArray): Result<Unit> {
        return try {
            val dataChannel = dataChannels[peerId]
                ?: return Result.failure(IllegalStateException("No data channel for peer: $peerId"))

            if (dataChannel.state() != DataChannel.State.OPEN) {
                return Result.failure(IllegalStateException("Data channel not open for peer: $peerId"))
            }

            val buffer = DataChannel.Buffer(ByteBuffer.wrap(data), true)
            val sent = dataChannel.send(buffer)

            if (sent) {
                Timber.d("Sent ${data.size} bytes to peer: $peerId")
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Failed to send data to peer: $peerId"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to send data to peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Close connection to a peer.
     */
    fun disconnectFromPeer(peerId: String) {
        dataChannels.remove(peerId)?.dispose()
        peerConnections.remove(peerId)?.close()

        scope.launch {
            _networkEvents.send(NetworkEvent.PeerDisconnected(peerId, "Manual disconnect"))
        }

        Timber.d("Disconnected from peer: $peerId")
    }

    /**
     * Close all connections.
     */
    fun shutdown() {
        dataChannels.values.forEach { it.dispose() }
        dataChannels.clear()

        peerConnections.values.forEach { it.close() }
        peerConnections.clear()

        if (isInitialized) {
            peerConnectionFactory.dispose()
            isInitialized = false
        }

        Timber.d("WebRTC shutdown complete")
    }

    /**
     * Create an offer for a peer connection.
     */
    suspend fun createOffer(peerId: String): Result<SessionDescription> {
        return try {
            val peerConnection = peerConnections[peerId]
                ?: return Result.failure(IllegalStateException("No peer connection for: $peerId"))

            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            }

            val sdpObserver = SdpObserverImpl()
            peerConnection.createOffer(sdpObserver, constraints)

            // Wait for offer to be created (simplified - in production use coroutines properly)
            val offer = sdpObserver.awaitSessionDescription()
            peerConnection.setLocalDescription(SdpObserverImpl(), offer)

            Result.success(offer)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create offer for peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Create an answer for a peer connection.
     */
    suspend fun createAnswer(peerId: String, offer: SessionDescription): Result<SessionDescription> {
        return try {
            val peerConnection = peerConnections[peerId]
                ?: return Result.failure(IllegalStateException("No peer connection for: $peerId"))

            peerConnection.setRemoteDescription(SdpObserverImpl(), offer)

            val constraints = MediaConstraints()
            val sdpObserver = SdpObserverImpl()
            peerConnection.createAnswer(sdpObserver, constraints)

            val answer = sdpObserver.awaitSessionDescription()
            peerConnection.setLocalDescription(SdpObserverImpl(), answer)

            Result.success(answer)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create answer for peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Set remote description.
     */
    fun setRemoteDescription(peerId: String, sessionDescription: SessionDescription): Result<Unit> {
        return try {
            val peerConnection = peerConnections[peerId]
                ?: return Result.failure(IllegalStateException("No peer connection for: $peerId"))

            peerConnection.setRemoteDescription(SdpObserverImpl(), sessionDescription)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set remote description for peer: $peerId")
            Result.failure(e)
        }
    }

    /**
     * Add ICE candidate.
     */
    fun addIceCandidate(peerId: String, iceCandidate: IceCandidate): Result<Unit> {
        return try {
            val peerConnection = peerConnections[peerId]
                ?: return Result.failure(IllegalStateException("No peer connection for: $peerId"))

            peerConnection.addIceCandidate(iceCandidate)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add ICE candidate for peer: $peerId")
            Result.failure(e)
        }
    }

    // ========== Observers ==========

    private inner class PeerConnectionObserver(private val peerId: String) : PeerConnection.Observer {
        override fun onIceCandidate(iceCandidate: IceCandidate?) {
            Timber.d("ICE candidate for $peerId: ${iceCandidate?.sdp}")
            // TODO: Send ICE candidate to peer via signaling
        }

        override fun onDataChannel(dataChannel: DataChannel?) {
            dataChannel?.let {
                Timber.d("Data channel received from $peerId")
                it.registerObserver(DataChannelObserver(peerId))
                dataChannels[peerId] = it
            }
        }

        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
            Timber.d("ICE connection state for $peerId: $newState")
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Timber.d("ICE connection receiving change for $peerId: $receiving")
        }

        override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>?) {
            Timber.d("ICE candidates removed for $peerId: ${iceCandidates?.size ?: 0}")
        }

        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
            newState?.let { state ->
                Timber.d("Peer connection state for $peerId: $state")
                scope.launch {
                    _connectionStates.value = _connectionStates.value + (peerId to state)
                }
            }
        }

        override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
            Timber.d("Signaling state for $peerId: $newState")
        }

        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
            Timber.d("ICE gathering state for $peerId: $newState")
        }

        override fun onAddStream(stream: MediaStream?) {}
        override fun onRemoveStream(stream: MediaStream?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
    }

    private inner class DataChannelObserver(private val peerId: String) : DataChannel.Observer {
        override fun onMessage(buffer: DataChannel.Buffer?) {
            buffer?.let {
                val data = ByteArray(it.data.remaining())
                it.data.get(data)

                scope.launch {
                    _incomingMessages.send(DataChannelMessage(peerId, data))
                }

                Timber.d("Received ${data.size} bytes from $peerId")
            }
        }

        override fun onStateChange() {
            val channel = dataChannels[peerId]
            Timber.d("Data channel state for $peerId: ${channel?.state()}")
        }

        override fun onBufferedAmountChange(amount: Long) {
            Timber.v("Data channel buffered amount for $peerId: $amount")
        }
    }

    private class SdpObserverImpl : SdpObserver {
        private var sessionDescription: SessionDescription? = null
        private val lock = Object()

        fun awaitSessionDescription(): SessionDescription {
            synchronized(lock) {
                while (sessionDescription == null) {
                    (lock as Object).wait(5000)
                }
                return sessionDescription!!
            }
        }

        override fun onCreateSuccess(sdp: SessionDescription?) {
            synchronized(lock) {
                sessionDescription = sdp
                (lock as Object).notifyAll()
            }
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {
            Timber.e("SDP create failure: $error")
        }
        override fun onSetFailure(error: String?) {
            Timber.e("SDP set failure: $error")
        }
    }
}

/**
 * Data channel message.
 */
data class DataChannelMessage(
    val peerId: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataChannelMessage

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
