package com.chain.app.data.repository

import com.chain.app.data.local.dao.CallDao
import com.chain.app.data.local.entity.toEntity
import com.chain.app.data.local.entity.toDomain
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.data.webrtc.CallManager
import com.chain.app.domain.model.Call
import com.chain.app.domain.model.CallSession
import com.chain.app.domain.model.CallStatus
import com.chain.app.domain.model.CallType
import com.chain.app.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CallRepository.
 * Manages call state, history, and integrates with CallManager for WebRTC operations.
 */
@Singleton
class CallRepositoryImpl @Inject constructor(
    private val callDao: CallDao,
    private val callManager: CallManager,
    private val userPreferences: UserPreferences
) : CallRepository {

    // Active call session state
    private val _activeCallSession = MutableStateFlow<CallSession?>(null)

    // Incoming call notifications
    private val _incomingCalls = MutableSharedFlow<Call>(replay = 0)

    init {
        // Initialize CallManager
        callManager.initialize()
    }

    override suspend fun initiateCall(
        peerId: String,
        callType: CallType
    ): Result<CallSession> {
        return try {
            val currentUserId = userPreferences.getUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Create call record
            val call = Call(
                id = UUID.randomUUID().toString(),
                chatId = peerId, // For 1-on-1 calls, chatId is the peer ID
                initiator = currentUserId,
                participants = listOf(currentUserId, peerId),
                type = callType,
                status = CallStatus.INITIATING,
                startTime = Date()
            )

            // Save to database
            callDao.insertCall(call.toEntity())

            // Start WebRTC call
            val sessionDescription = callManager.startCall(
                peerId = peerId,
                callType = callType,
                onIceCandidate = { candidate ->
                    // TODO: Send ICE candidate to peer via P2P
                    Timber.d("ICE candidate: $candidate")
                },
                onRemoteStream = { stream ->
                    Timber.d("Remote stream received")
                }
            ).getOrThrow()

            // Update status to ringing
            callDao.updateCallStatus(call.id, CallStatus.RINGING.name)

            // Create call session
            val callSession = CallSession(
                call = call.copy(status = CallStatus.RINGING),
                isMuted = false,
                isVideoEnabled = callType == CallType.VIDEO,
                isSpeakerEnabled = false
            )

            _activeCallSession.value = callSession

            Timber.d("Call initiated: ${call.id}")
            Result.success(callSession)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initiate call")
            Result.failure(e)
        }
    }

    override suspend fun acceptCall(callId: String): Result<CallSession> {
        return try {
            val call = callDao.getCall(callId)?.toDomain()
                ?: return Result.failure(Exception("Call not found"))

            if (call.status != CallStatus.RINGING) {
                return Result.failure(Exception("Call is not ringing"))
            }

            // TODO: Get SDP offer from P2P message
            // For now, this is a placeholder
            val offer = org.webrtc.SessionDescription(
                org.webrtc.SessionDescription.Type.OFFER,
                ""
            )

            // Answer call via CallManager
            val answer = callManager.answerCall(
                offer = offer,
                onIceCandidate = { candidate ->
                    // TODO: Send ICE candidate to peer via P2P
                    Timber.d("ICE candidate: $candidate")
                },
                onRemoteStream = { stream ->
                    Timber.d("Remote stream received")
                }
            ).getOrThrow()

            // Update call status
            callDao.updateCallStatus(callId, CallStatus.CONNECTED.name)

            val updatedCall = call.copy(status = CallStatus.CONNECTED)
            val callSession = CallSession(
                call = updatedCall,
                isMuted = false,
                isVideoEnabled = call.type == CallType.VIDEO,
                isSpeakerEnabled = false
            )

            _activeCallSession.value = callSession

            Timber.d("Call accepted: $callId")
            Result.success(callSession)
        } catch (e: Exception) {
            Timber.e(e, "Failed to accept call")
            Result.failure(e)
        }
    }

    override suspend fun rejectCall(callId: String): Result<Unit> {
        return try {
            val call = callDao.getCall(callId)
                ?: return Result.failure(Exception("Call not found"))

            // Update status
            callDao.updateCallStatus(callId, CallStatus.REJECTED.name)
            callDao.endCall(
                callId = callId,
                endTime = System.currentTimeMillis(),
                duration = 0
            )

            // TODO: Send rejection signal to peer via P2P

            Timber.d("Call rejected: $callId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to reject call")
            Result.failure(e)
        }
    }

    override suspend fun endCall(callId: String): Result<Unit> {
        return try {
            val call = callDao.getCall(callId)?.toDomain()
                ?: return Result.failure(Exception("Call not found"))

            val endTime = System.currentTimeMillis()
            val duration = endTime - call.startTime.time

            // End WebRTC call
            callManager.endCall().getOrThrow()

            // Update database
            callDao.updateCallStatus(callId, CallStatus.ENDED.name)
            callDao.endCall(
                callId = callId,
                endTime = endTime,
                duration = duration
            )

            // Clear active session
            _activeCallSession.value = null

            // TODO: Send end signal to peer via P2P

            Timber.d("Call ended: $callId, duration: ${duration}ms")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to end call")
            Result.failure(e)
        }
    }

    override suspend fun toggleMute(callId: String): Result<Boolean> {
        return try {
            val currentSession = _activeCallSession.value
                ?: return Result.failure(Exception("No active call"))

            if (currentSession.call.id != callId) {
                return Result.failure(Exception("Call ID mismatch"))
            }

            val newMuteState = !currentSession.isMuted
            callManager.setAudioEnabled(!newMuteState).getOrThrow()

            _activeCallSession.value = currentSession.copy(isMuted = newMuteState)

            Timber.d("Audio ${if (newMuteState) "muted" else "unmuted"}")
            Result.success(newMuteState)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle mute")
            Result.failure(e)
        }
    }

    override suspend fun toggleVideo(callId: String): Result<Boolean> {
        return try {
            val currentSession = _activeCallSession.value
                ?: return Result.failure(Exception("No active call"))

            if (currentSession.call.id != callId) {
                return Result.failure(Exception("Call ID mismatch"))
            }

            val newVideoState = !currentSession.isVideoEnabled
            callManager.setVideoEnabled(newVideoState).getOrThrow()

            _activeCallSession.value = currentSession.copy(isVideoEnabled = newVideoState)

            Timber.d("Video ${if (newVideoState) "enabled" else "disabled"}")
            Result.success(newVideoState)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle video")
            Result.failure(e)
        }
    }

    override suspend fun toggleSpeaker(callId: String): Result<Boolean> {
        return try {
            val currentSession = _activeCallSession.value
                ?: return Result.failure(Exception("No active call"))

            if (currentSession.call.id != callId) {
                return Result.failure(Exception("Call ID mismatch"))
            }

            val newSpeakerState = !currentSession.isSpeakerEnabled
            callManager.setSpeakerEnabled(newSpeakerState).getOrThrow()

            _activeCallSession.value = currentSession.copy(isSpeakerEnabled = newSpeakerState)

            Timber.d("Speaker ${if (newSpeakerState) "enabled" else "disabled"}")
            Result.success(newSpeakerState)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle speaker")
            Result.failure(e)
        }
    }

    override suspend fun switchCamera(callId: String): Result<Unit> {
        return try {
            val currentSession = _activeCallSession.value
                ?: return Result.failure(Exception("No active call"))

            if (currentSession.call.id != callId) {
                return Result.failure(Exception("Call ID mismatch"))
            }

            callManager.switchCamera().getOrThrow()

            Timber.d("Camera switched")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to switch camera")
            Result.failure(e)
        }
    }

    override fun getCallHistory(): Flow<List<Call>> {
        return callDao.getAllCalls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeIncomingCalls(): Flow<Call> {
        return _incomingCalls.asSharedFlow()
    }

    override fun observeCallSession(callId: String): Flow<CallSession> {
        return _activeCallSession.asStateFlow().map { session ->
            session ?: throw IllegalStateException("No active call session")
        }
    }

    /**
     * Internal method to handle incoming call from P2P.
     * Called when a call signal is received.
     */
    suspend fun handleIncomingCall(
        callId: String,
        fromPeerId: String,
        callType: CallType
    ) {
        try {
            val currentUserId = userPreferences.getUserId()
                ?: throw Exception("User not authenticated")

            val call = Call(
                id = callId,
                chatId = fromPeerId,
                initiator = fromPeerId,
                participants = listOf(currentUserId, fromPeerId),
                type = callType,
                status = CallStatus.RINGING,
                startTime = Date()
            )

            // Save to database
            callDao.insertCall(call.toEntity())

            // Emit incoming call
            _incomingCalls.emit(call)

            Timber.d("Incoming call received: $callId from $fromPeerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle incoming call")
        }
    }
}
