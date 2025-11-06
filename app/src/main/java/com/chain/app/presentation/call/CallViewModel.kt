package com.chain.app.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.domain.model.Call
import com.chain.app.domain.model.CallSession
import com.chain.app.domain.model.CallType
import com.chain.app.domain.model.Contact
import com.chain.app.domain.repository.CallRepository
import com.chain.app.domain.repository.ContactRepository
import com.chain.app.domain.usecase.call.AcceptCallUseCase
import com.chain.app.domain.usecase.call.EndCallUseCase
import com.chain.app.domain.usecase.call.InitiateCallUseCase
import com.chain.app.domain.usecase.call.RejectCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CallUiState {
    object Idle : CallUiState()
    object Initiating : CallUiState()
    data class Ringing(val call: Call) : CallUiState()
    data class InCall(val session: CallSession, val duration: String) : CallUiState()
    data class Ended(val duration: String) : CallUiState()
    data class Error(val message: String) : CallUiState()
}

@HiltViewModel
class CallViewModel @Inject constructor(
    private val initiateCallUseCase: InitiateCallUseCase,
    private val acceptCallUseCase: AcceptCallUseCase,
    private val endCallUseCase: EndCallUseCase,
    private val rejectCallUseCase: RejectCallUseCase,
    private val callRepository: CallRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isVideoOn = MutableStateFlow(false)
    val isVideoOn: StateFlow<Boolean> = _isVideoOn.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _callParticipants = MutableStateFlow<List<String>>(emptyList())
    val callParticipants: StateFlow<List<String>> = _callParticipants.asStateFlow()

    private var currentCallId: String? = null
    private var callStartTime: Long = 0
    private var durationJob: Job? = null

    init {
        observeIncomingCalls()
        loadContacts()
    }

    /**
     * Initiate a call with a peer.
     */
    fun initiateCall(peerId: String, callType: CallType) {
        viewModelScope.launch {
            _uiState.value = CallUiState.Initiating

            initiateCallUseCase(peerId, callType).fold(
                onSuccess = { session ->
                    currentCallId = session.call.id
                    callStartTime = session.call.startTime.time
                    _uiState.value = CallUiState.Ringing(session.call)

                    // Update state based on session
                    _isMuted.value = session.isMuted
                    _isSpeakerOn.value = session.isSpeakerEnabled
                    _isVideoOn.value = session.isVideoEnabled

                    // Start duration timer when call connects
                    startDurationTimer()
                },
                onFailure = { error ->
                    _uiState.value = CallUiState.Error(
                        error.message ?: "Failed to initiate call"
                    )
                }
            )
        }
    }

    /**
     * Accept an incoming call.
     */
    fun acceptCall() {
        viewModelScope.launch {
            val callId = (uiState.value as? CallUiState.Ringing)?.call?.id
                ?: return@launch

            acceptCallUseCase(callId).fold(
                onSuccess = { session ->
                    currentCallId = session.call.id
                    callStartTime = session.call.startTime.time

                    _isMuted.value = session.isMuted
                    _isSpeakerOn.value = session.isSpeakerEnabled
                    _isVideoOn.value = session.isVideoEnabled

                    startDurationTimer()
                },
                onFailure = { error ->
                    _uiState.value = CallUiState.Error(
                        error.message ?: "Failed to accept call"
                    )
                }
            )
        }
    }

    /**
     * Reject an incoming call.
     */
    fun rejectCall() {
        viewModelScope.launch {
            val callId = (uiState.value as? CallUiState.Ringing)?.call?.id
                ?: return@launch

            rejectCallUseCase(callId).fold(
                onSuccess = {
                    _uiState.value = CallUiState.Idle
                    currentCallId = null
                },
                onFailure = { error ->
                    _uiState.value = CallUiState.Error(
                        error.message ?: "Failed to reject call"
                    )
                }
            )
        }
    }

    /**
     * End the active call.
     */
    fun endCall() {
        viewModelScope.launch {
            val callId = currentCallId ?: return@launch

            durationJob?.cancel()

            endCallUseCase(callId).fold(
                onSuccess = {
                    val duration = formatDuration(System.currentTimeMillis() - callStartTime)
                    _uiState.value = CallUiState.Ended(duration)
                    currentCallId = null
                },
                onFailure = { error ->
                    _uiState.value = CallUiState.Error(
                        error.message ?: "Failed to end call"
                    )
                }
            )
        }
    }

    /**
     * Toggle mute.
     */
    fun toggleMute() {
        viewModelScope.launch {
            val callId = currentCallId ?: return@launch

            callRepository.toggleMute(callId).fold(
                onSuccess = { muted ->
                    _isMuted.value = muted
                },
                onFailure = { error ->
                    // Show error but don't interrupt call
                }
            )
        }
    }

    /**
     * Toggle speaker.
     */
    fun toggleSpeaker() {
        viewModelScope.launch {
            val callId = currentCallId ?: return@launch

            callRepository.toggleSpeaker(callId).fold(
                onSuccess = { speakerOn ->
                    _isSpeakerOn.value = speakerOn
                },
                onFailure = { error ->
                    // Show error but don't interrupt call
                }
            )
        }
    }

    /**
     * Toggle video.
     */
    fun toggleVideo() {
        viewModelScope.launch {
            val callId = currentCallId ?: return@launch

            callRepository.toggleVideo(callId).fold(
                onSuccess = { videoOn ->
                    _isVideoOn.value = videoOn
                },
                onFailure = { error ->
                    // Show error but don't interrupt call
                }
            )
        }
    }

    /**
     * Switch camera (front/back).
     */
    fun switchCamera() {
        viewModelScope.launch {
            val callId = currentCallId ?: return@launch

            callRepository.switchCamera(callId).fold(
                onSuccess = {
                    // Camera switched successfully
                },
                onFailure = { error ->
                    // Show error but don't interrupt call
                }
            )
        }
    }

    private fun observeIncomingCalls() {
        viewModelScope.launch {
            callRepository.observeIncomingCalls().collect { call ->
                if (uiState.value is CallUiState.Idle) {
                    _uiState.value = CallUiState.Ringing(call)
                }
            }
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val duration = formatDuration(System.currentTimeMillis() - callStartTime)
                val currentState = uiState.value
                if (currentState is CallUiState.InCall) {
                    _uiState.value = currentState.copy(duration = duration)
                } else {
                    // Transition from Ringing to InCall
                    val session = CallSession(
                        call = (currentState as? CallUiState.Ringing)?.call ?: continue,
                        isMuted = _isMuted.value,
                        isVideoEnabled = _isVideoOn.value,
                        isSpeakerEnabled = _isSpeakerOn.value
                    )
                    _uiState.value = CallUiState.InCall(session, duration)
                }
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contactList ->
                _contacts.value = contactList.filter { !it.isBlocked }
            }
        }
    }

    /**
     * Add a participant to the active call.
     */
    fun addParticipant(contactId: String) {
        viewModelScope.launch {
            val contact = contactRepository.getContactById(contactId) ?: return@launch

            // Add to participants list
            if (!_callParticipants.value.contains(contact.userId)) {
                _callParticipants.value = _callParticipants.value + contact.userId

                // TODO: Implement WebRTC multi-party call logic
                // This would involve creating new peer connections for each participant
                // and managing the media streams accordingly
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
    }
}
