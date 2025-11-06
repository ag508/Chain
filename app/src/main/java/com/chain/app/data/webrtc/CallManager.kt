package com.chain.app.data.webrtc

import android.content.Context
import android.media.AudioManager
import com.chain.app.domain.model.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.webrtc.*
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Participant in a multi-party call.
 */
data class CallParticipant(
    val peerId: String,
    val peerConnection: PeerConnection,
    var remoteAudioTrack: AudioTrack? = null,
    var remoteVideoTrack: VideoTrack? = null,
    var isAudioEnabled: Boolean = true,
    var isVideoEnabled: Boolean = false
)

/**
 * Manages WebRTC audio/video streams for voice and video calls.
 * Supports multi-party calls using mesh architecture.
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Peer connection factory
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioSource: AudioSource
    private lateinit var videoSource: VideoSource
    private lateinit var videoCapturer: VideoCapturer

    // Multi-party call participants (mesh architecture)
    private val participants = ConcurrentHashMap<String, CallParticipant>()

    // Local media tracks (shared across all participants)
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    // Callbacks for signaling
    private var onIceCandidateCallback: ((peerId: String, candidate: IceCandidate) -> Unit)? = null
    private var onRemoteStreamCallback: ((peerId: String, stream: MediaStream) -> Unit)? = null
    private var onParticipantLeftCallback: ((peerId: String) -> Unit)? = null

    // Audio manager for routing
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // STUN/TURN servers
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
    )

    private var isInitialized = false
    private var currentCallType: CallType = CallType.VOICE

    /**
     * Initialize WebRTC for calls.
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

            val eglBase = EglBase.create()
            val encoderFactory = DefaultVideoEncoderFactory(
                eglBase.eglBaseContext,
                true,
                true
            )
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory()

            // Create audio source
            val audioConstraints = MediaConstraints().apply {
                optional.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                optional.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                optional.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                optional.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            }
            audioSource = peerConnectionFactory.createAudioSource(audioConstraints)

            // Create video source
            videoSource = peerConnectionFactory.createVideoSource(false)

            isInitialized = true
            Timber.d("CallManager initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize CallManager")
            Result.failure(e)
        }
    }

    /**
     * Set callbacks for signaling events.
     */
    fun setCallbacks(
        onIceCandidate: (peerId: String, candidate: IceCandidate) -> Unit,
        onRemoteStream: (peerId: String, stream: MediaStream) -> Unit,
        onParticipantLeft: (peerId: String) -> Unit
    ) {
        this.onIceCandidateCallback = onIceCandidate
        this.onRemoteStreamCallback = onRemoteStream
        this.onParticipantLeftCallback = onParticipantLeft
    }

    /**
     * Start a call (voice or video) - creates connection to first participant.
     */
    suspend fun startCall(
        peerId: String,
        callType: CallType,
        onIceCandidate: (IceCandidate) -> Unit,
        onRemoteStream: (MediaStream) -> Unit
    ): Result<SessionDescription> {
        // Set legacy callbacks for backward compatibility
        this.onIceCandidateCallback = { id, candidate ->
            if (id == peerId) onIceCandidate(candidate)
        }
        this.onRemoteStreamCallback = { id, stream ->
            if (id == peerId) onRemoteStream(stream)
        }

        currentCallType = callType
        return addParticipant(peerId, callType, isInitiator = true)
    }

    /**
     * Add a new participant to the call (for multi-party calls).
     */
    suspend fun addParticipant(
        peerId: String,
        callType: CallType = currentCallType,
        isInitiator: Boolean = true
    ): Result<SessionDescription> {
        return try {
            if (!isInitialized) {
                initialize().getOrThrow()
            }

            // Check if participant already exists
            if (participants.containsKey(peerId)) {
                return Result.failure(Exception("Participant $peerId already in call"))
            }

            // Create local tracks if not created yet (shared across all participants)
            if (localAudioTrack == null) {
                localAudioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
            }

            if (callType == CallType.VIDEO && localVideoTrack == null) {
                initializeVideoCapture()
                localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
            }

            // Create peer connection for this participant
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            }

            val peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfig,
                createPeerConnectionObserver(peerId)
            ) ?: return Result.failure(Exception("Failed to create peer connection for $peerId"))

            // Add local tracks to this peer connection
            peerConnection.addTrack(localAudioTrack)
            if (callType == CallType.VIDEO && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack)
            }

            // Store participant
            val participant = CallParticipant(
                peerId = peerId,
                peerConnection = peerConnection
            )
            participants[peerId] = participant

            // Create offer if we're the initiator
            val sdpConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                    if (callType == CallType.VIDEO) "true" else "false"))
            }

            val sdp = if (isInitiator) {
                val offer = peerConnection.createOffer(sdpConstraints)
                    ?: return Result.failure(Exception("Failed to create offer for $peerId"))
                peerConnection.setLocalDescription(offer)
                offer
            } else {
                // For answer, we'll receive the offer later via handleRemoteOffer
                return Result.failure(Exception("Not initiator - call handleRemoteOffer first"))
            }

            // Configure audio routing on first participant
            if (participants.size == 1) {
                configureAudioRouting()
            }

            Timber.d("Added participant: $peerId (total: ${participants.size})")
            Result.success(sdp)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add participant $peerId")
            Result.failure(e)
        }
    }

    /**
     * Handle remote offer from a participant and create answer.
     */
    suspend fun handleRemoteOffer(
        peerId: String,
        offer: SessionDescription,
        callType: CallType = currentCallType
    ): Result<SessionDescription> {
        return try {
            if (!isInitialized) {
                initialize().getOrThrow()
            }

            // Create local tracks if needed
            if (localAudioTrack == null) {
                localAudioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
            }

            if (callType == CallType.VIDEO && localVideoTrack == null) {
                initializeVideoCapture()
                localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
            }

            // Create peer connection
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
            val peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfig,
                createPeerConnectionObserver(peerId)
            ) ?: return Result.failure(Exception("Failed to create peer connection"))

            // Add local tracks
            peerConnection.addTrack(localAudioTrack)
            if (callType == CallType.VIDEO && localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack)
            }

            // Store participant
            val participant = CallParticipant(
                peerId = peerId,
                peerConnection = peerConnection
            )
            participants[peerId] = participant

            // Set remote description (offer)
            peerConnection.setRemoteDescription(offer)

            // Create answer
            val answerConstraints = MediaConstraints()
            val answer = peerConnection.createAnswer(answerConstraints)
                ?: return Result.failure(Exception("Failed to create answer"))

            peerConnection.setLocalDescription(answer)

            // Configure audio routing on first participant
            if (participants.size == 1) {
                configureAudioRouting()
            }

            Timber.d("Handled offer from $peerId, created answer")
            Result.success(answer)
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle offer from $peerId")
            Result.failure(e)
        }
    }

    /**
     * Handle remote answer from a participant.
     */
    fun handleRemoteAnswer(peerId: String, answer: SessionDescription): Result<Unit> {
        return try {
            val participant = participants[peerId]
                ?: return Result.failure(Exception("Participant $peerId not found"))

            participant.peerConnection.setRemoteDescription(answer)
            Timber.d("Set remote answer for $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set remote answer for $peerId")
            Result.failure(e)
        }
    }

    /**
     * Add ICE candidate for a specific participant.
     */
    fun addIceCandidate(peerId: String, candidate: IceCandidate): Result<Unit> {
        return try {
            val participant = participants[peerId]
                ?: return Result.failure(Exception("Participant $peerId not found"))

            participant.peerConnection.addIceCandidate(candidate)
            Timber.d("Added ICE candidate for $peerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add ICE candidate for $peerId")
            Result.failure(e)
        }
    }

    /**
     * Remove a participant from the call.
     */
    fun removeParticipant(peerId: String): Result<Unit> {
        return try {
            val participant = participants.remove(peerId)
                ?: return Result.failure(Exception("Participant $peerId not found"))

            participant.peerConnection.close()
            Timber.d("Removed participant: $peerId (remaining: ${participants.size})")

            onParticipantLeftCallback?.invoke(peerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove participant $peerId")
            Result.failure(e)
        }
    }

    /**
     * Get list of current participants.
     */
    fun getParticipants(): List<String> {
        return participants.keys.toList()
    }

    /**
     * Legacy method: Answer an incoming call (backward compatibility).
     */
    suspend fun answerCall(
        offer: SessionDescription,
        onIceCandidate: (IceCandidate) -> Unit,
        onRemoteStream: (MediaStream) -> Unit
    ): Result<SessionDescription> {
        // Set legacy callbacks
        this.onIceCandidateCallback = { _, candidate -> onIceCandidate(candidate) }
        this.onRemoteStreamCallback = { _, stream -> onRemoteStream(stream) }

        // Use first peer ID as "caller"
        val callerId = "caller"
        return handleRemoteOffer(callerId, offer, CallType.VOICE)
    }

    /**
     * End the active call - closes all peer connections.
     */
    fun endCall(): Result<Unit> {
        return try {
            // Close all participant connections
            participants.values.forEach { participant ->
                try {
                    participant.peerConnection.close()
                } catch (e: Exception) {
                    Timber.e(e, "Error closing connection for ${participant.peerId}")
                }
            }
            participants.clear()

            // Disable and clean up local tracks
            localAudioTrack?.setEnabled(false)
            localVideoTrack?.setEnabled(false)

            // Only stop video capture if it was initialized
            if (::videoCapturer.isInitialized) {
                videoCapturer.stopCapture()
            }

            localAudioTrack = null
            localVideoTrack = null

            // Reset audio routing
            audioManager.mode = AudioManager.MODE_NORMAL
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = false

            Timber.d("Call ended, all participants removed")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to end call")
            Result.failure(e)
        }
    }

    /**
     * Toggle audio mute (affects all participants).
     */
    fun setAudioEnabled(enabled: Boolean): Result<Unit> {
        return try {
            localAudioTrack?.setEnabled(enabled)
            Timber.d("Audio ${if (enabled) "enabled" else "disabled"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle audio")
            Result.failure(e)
        }
    }

    /**
     * Toggle video (affects all participants).
     */
    fun setVideoEnabled(enabled: Boolean): Result<Unit> {
        return try {
            if (enabled && localVideoTrack == null) {
                initializeVideoCapture()
                localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
                // Add video track to all existing connections
                participants.values.forEach { participant ->
                    participant.peerConnection.addTrack(localVideoTrack)
                }
            }

            localVideoTrack?.setEnabled(enabled)
            Timber.d("Video ${if (enabled) "enabled" else "disabled"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle video")
            Result.failure(e)
        }
    }

    /**
     * Toggle speaker.
     */
    fun setSpeakerEnabled(enabled: Boolean): Result<Unit> {
        return try {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = enabled
            Timber.d("Speaker ${if (enabled) "enabled" else "disabled"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle speaker")
            Result.failure(e)
        }
    }

    /**
     * Switch camera (front/back).
     */
    fun switchCamera(): Result<Unit> {
        return try {
            if (::videoCapturer.isInitialized && videoCapturer is CameraVideoCapturer) {
                (videoCapturer as CameraVideoCapturer).switchCamera(null)
                Timber.d("Camera switched")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to switch camera")
            Result.failure(e)
        }
    }

    /**
     * Create peer connection observer for a specific participant.
     */
    private fun createPeerConnectionObserver(peerId: String): PeerConnection.Observer {
        return object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    onIceCandidateCallback?.invoke(peerId, it)
                }
            }

            override fun onAddStream(stream: MediaStream?) {
                stream?.let {
                    // Store remote tracks
                    val participant = participants[peerId]
                    if (participant != null) {
                        if (it.audioTracks.isNotEmpty()) {
                            participant.remoteAudioTrack = it.audioTracks[0]
                            participant.isAudioEnabled = true
                        }
                        if (it.videoTracks.isNotEmpty()) {
                            participant.remoteVideoTrack = it.videoTracks[0]
                            participant.isVideoEnabled = true
                        }
                    }

                    onRemoteStreamCallback?.invoke(peerId, it)
                }
            }

            override fun onRemoveStream(stream: MediaStream?) {
                Timber.d("Stream removed from $peerId")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Timber.d("Connection state changed for $peerId: $newState")

                // Handle disconnection
                if (newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                    newState == PeerConnection.PeerConnectionState.FAILED ||
                    newState == PeerConnection.PeerConnectionState.CLOSED) {
                    removeParticipant(peerId)
                }
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Timber.d("ICE connection state for $peerId: $state")
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onDataChannel(dataChannel: DataChannel?) {}
            override fun onRenegotiationNeeded() {
                Timber.d("Renegotiation needed for $peerId")
            }
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
        }
    }

    private fun initializeVideoCapture() {
        val camera2Enumerator = Camera2Enumerator(context)
        val deviceNames = camera2Enumerator.deviceNames

        // Find front camera
        val frontCamera = deviceNames.find { camera2Enumerator.isFrontFacing(it) }

        videoCapturer = camera2Enumerator.createCapturer(
            frontCamera ?: deviceNames[0],
            null
        )

        videoCapturer.initialize(
            SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext),
            context,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(640, 480, 30)
    }

    private fun configureAudioRouting() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = false
    }
}

// Extension functions for synchronous SDP operations
private suspend fun PeerConnection.createOffer(constraints: MediaConstraints): SessionDescription? {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                cont.resume(sdp, null)
            }

            override fun onCreateFailure(error: String?) {
                cont.resumeWith(Result.failure(Exception(error ?: "Unknown error")))
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
}

private suspend fun PeerConnection.createAnswer(constraints: MediaConstraints): SessionDescription? {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                cont.resume(sdp, null)
            }

            override fun onCreateFailure(error: String?) {
                cont.resumeWith(Result.failure(Exception(error ?: "Unknown error")))
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
}

private fun PeerConnection.setLocalDescription(sdp: SessionDescription) {
    setLocalDescription(object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {
            Timber.d("Local description set successfully")
        }
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(error: String?) {
            Timber.e("Failed to set local description: $error")
        }
    }, sdp)
}

private fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    setRemoteDescription(object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {
            Timber.d("Remote description set successfully")
        }
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(error: String?) {
            Timber.e("Failed to set remote description: $error")
        }
    }, sdp)
}
