package com.chain.app.domain.repository

import com.chain.app.domain.model.Call
import com.chain.app.domain.model.CallSession
import com.chain.app.domain.model.CallType
import kotlinx.coroutines.flow.Flow
import org.webrtc.VideoTrack

/**
 * Repository interface for call operations.
 */
interface CallRepository {
    /**
     * Initiate a call with a user.
     */
    suspend fun initiateCall(peerId: String, callType: CallType): Result<CallSession>

    /**
     * Accept an incoming call.
     */
    suspend fun acceptCall(callId: String): Result<CallSession>

    /**
     * Reject an incoming call.
     */
    suspend fun rejectCall(callId: String): Result<Unit>

    /**
     * End an active call.
     */
    suspend fun endCall(callId: String): Result<Unit>

    /**
     * Toggle mute status.
     */
    suspend fun toggleMute(callId: String): Result<Boolean>

    /**
     * Toggle video status.
     */
    suspend fun toggleVideo(callId: String): Result<Boolean>

    /**
     * Toggle speaker status.
     */
    suspend fun toggleSpeaker(callId: String): Result<Boolean>

    /**
     * Switch camera (front/back).
     */
    suspend fun switchCamera(callId: String): Result<Unit>

    /**
     * Add a participant to an active call.
     */
    suspend fun addParticipantToCall(callId: String, peerId: String, callType: CallType): Result<Unit>

    /**
     * Remove a participant from an active call.
     */
    suspend fun removeParticipantFromCall(callId: String, peerId: String): Result<Unit>

    /**
     * Get list of participants in a call.
     */
    suspend fun getCallParticipants(callId: String): Result<List<String>>

    /**
     * Get call history.
     */
    fun getCallHistory(): Flow<List<Call>>

    /**
     * Observe incoming calls.
     */
    fun observeIncomingCalls(): Flow<Call>

    /**
     * Observe active call session.
     */
    fun observeCallSession(callId: String): Flow<CallSession>

    /**
     * Get local video track for rendering in UI.
     */
    fun getLocalVideoTrack(): VideoTrack?

    /**
     * Get remote video track for a specific peer.
     */
    fun getRemoteVideoTrack(peerId: String): VideoTrack?

    /**
     * Get all remote video tracks (for multi-party video calls).
     */
    fun getAllRemoteVideoTracks(): List<Pair<String, VideoTrack>>
}
