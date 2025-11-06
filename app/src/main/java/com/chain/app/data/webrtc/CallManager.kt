package com.chain.app.data.webrtc

import android.content.Context
import android.media.AudioManager
import com.chain.app.domain.model.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WebRTC audio/video streams for voice and video calls.
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Peer connection factory
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioSource: AudioSource
    private lateinit var videoSource: VideoSource
    private lateinit var videoCapturer: VideoCapturer

    // Active peer connection
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null

    // Audio manager for routing
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // Call state
    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(false)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isSpeakerEnabled = MutableStateFlow(false)
    val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled.asStateFlow()

    private val _connectionState = MutableStateFlow<PeerConnection.PeerConnectionState?>(null)
    val connectionState: StateFlow<PeerConnection.PeerConnectionState?> = _connectionState.asStateFlow()

    // STUN/TURN servers
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
    )

    private var isInitialized = false

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
     * Start a call (voice or video).
     */
    suspend fun startCall(
        peerId: String,
        callType: CallType,
        onIceCandidate: (IceCandidate) -> Unit,
        onRemoteStream: (MediaStream) -> Unit
    ): Result<SessionDescription> {
        return try {
            if (!isInitialized) {
                initialize().getOrThrow()
            }

            // Create peer connection
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            }

            peerConnection = peerConnectionFactory.createPeerConnection(
                rtcConfig,
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate?) {
                        candidate?.let { onIceCandidate(it) }
                    }

                    override fun onAddStream(stream: MediaStream?) {
                        stream?.let {
                            if (it.videoTracks.isNotEmpty()) {
                                remoteVideoTrack = it.videoTracks[0]
                            }
                            onRemoteStream(it)
                        }
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                        _connectionState.value = newState
                        Timber.d("Connection state changed: $newState")
                    }

                    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
                    override fun onIceConnectionReceivingChange(p0: Boolean) {}
                    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                    override fun onRemoveStream(p0: MediaStream?) {}
                    override fun onDataChannel(p0: DataChannel?) {}
                    override fun onRenegotiationNeeded() {}
                    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
                }
            ) ?: return Result.failure(Exception("Failed to create peer connection"))

            // Add local audio track
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
            peerConnection?.addTrack(localAudioTrack)

            // Add local video track if video call
            if (callType == CallType.VIDEO) {
                initializeVideoCapture()
                localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
                peerConnection?.addTrack(localVideoTrack)
                _isVideoEnabled.value = true
            }

            // Create offer
            val offerConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (callType == CallType.VIDEO) "true" else "false"))
            }

            val offer = peerConnection?.createOffer(offerConstraints)
                ?: return Result.failure(Exception("Failed to create offer"))

            peerConnection?.setLocalDescription(offer)

            // Configure audio routing
            configureAudioRouting()

            Timber.d("Call started with peer: $peerId")
            Result.success(offer)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start call")
            Result.failure(e)
        }
    }

    /**
     * Answer an incoming call.
     */
    suspend fun answerCall(
        offer: SessionDescription,
        onIceCandidate: (IceCandidate) -> Unit,
        onRemoteStream: (MediaStream) -> Unit
    ): Result<SessionDescription> {
        return try {
            if (!isInitialized) {
                initialize().getOrThrow()
            }

            // Create peer connection if not exists
            if (peerConnection == null) {
                val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
                peerConnection = peerConnectionFactory.createPeerConnection(
                    rtcConfig,
                    object : PeerConnection.Observer {
                        override fun onIceCandidate(candidate: IceCandidate?) {
                            candidate?.let { onIceCandidate(it) }
                        }

                        override fun onAddStream(stream: MediaStream?) {
                            stream?.let { onRemoteStream(it) }
                        }

                        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                            _connectionState.value = newState
                        }

                        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
                        override fun onIceConnectionReceivingChange(p0: Boolean) {}
                        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                        override fun onRemoveStream(p0: MediaStream?) {}
                        override fun onDataChannel(p0: DataChannel?) {}
                        override fun onRenegotiationNeeded() {}
                        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
                    }
                )
            }

            // Set remote description
            peerConnection?.setRemoteDescription(offer)

            // Add local tracks
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
            peerConnection?.addTrack(localAudioTrack)

            // Create answer
            val answerConstraints = MediaConstraints()
            val answer = peerConnection?.createAnswer(answerConstraints)
                ?: return Result.failure(Exception("Failed to create answer"))

            peerConnection?.setLocalDescription(answer)

            configureAudioRouting()

            Timber.d("Call answered")
            Result.success(answer)
        } catch (e: Exception) {
            Timber.e(e, "Failed to answer call")
            Result.failure(e)
        }
    }

    /**
     * End the active call.
     */
    fun endCall(): Result<Unit> {
        return try {
            localAudioTrack?.setEnabled(false)
            localVideoTrack?.setEnabled(false)
            videoCapturer.stopCapture()

            peerConnection?.close()
            peerConnection = null

            localAudioTrack = null
            localVideoTrack = null
            remoteVideoTrack = null

            _connectionState.value = null
            _isAudioEnabled.value = true
            _isVideoEnabled.value = false
            _isSpeakerEnabled.value = false

            // Reset audio routing
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false

            Timber.d("Call ended")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to end call")
            Result.failure(e)
        }
    }

    /**
     * Toggle audio mute.
     */
    fun setAudioEnabled(enabled: Boolean): Result<Unit> {
        return try {
            localAudioTrack?.setEnabled(enabled)
            _isAudioEnabled.value = enabled
            Timber.d("Audio ${if (enabled) "enabled" else "disabled"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle audio")
            Result.failure(e)
        }
    }

    /**
     * Toggle video.
     */
    fun setVideoEnabled(enabled: Boolean): Result<Unit> {
        return try {
            if (enabled && localVideoTrack == null) {
                initializeVideoCapture()
                localVideoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
                peerConnection?.addTrack(localVideoTrack)
            }

            localVideoTrack?.setEnabled(enabled)
            _isVideoEnabled.value = enabled
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
            audioManager.isSpeakerphoneOn = enabled
            _isSpeakerEnabled.value = enabled
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
            if (videoCapturer is CameraVideoCapturer) {
                (videoCapturer as CameraVideoCapturer).switchCamera(null)
                Timber.d("Camera switched")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to switch camera")
            Result.failure(e)
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
        audioManager.isSpeakerphoneOn = false
    }

    /**
     * Add ICE candidate.
     */
    fun addIceCandidate(candidate: IceCandidate): Result<Unit> {
        return try {
            peerConnection?.addIceCandidate(candidate)
            Timber.d("ICE candidate added")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add ICE candidate")
            Result.failure(e)
        }
    }

    /**
     * Set remote description.
     */
    fun setRemoteDescription(sdp: SessionDescription): Result<Unit> {
        return try {
            peerConnection?.setRemoteDescription(sdp)
            Timber.d("Remote description set")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set remote description")
            Result.failure(e)
        }
    }
}

// Extension function to create offer/answer synchronously
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
