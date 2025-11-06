package com.chain.app.presentation.call

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.domain.model.CallType
import com.chain.app.presentation.theme.*

// Define vibrant red color for end call
private val VibrantRed = Color(0xFFFF3B30)
private val OnlineGreen = Color(0xFF34C759)
private val ReconnectingRed = Color(0xFFFF3B30)

@Composable
fun VoiceCallScreen(
    peerId: String?,
    isIncoming: Boolean = false,
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val participants by viewModel.callParticipants.collectAsState()

    var showContactPicker by remember { mutableStateOf(false) }

    // Initiate call if outgoing
    LaunchedEffect(peerId, isIncoming) {
        if (!isIncoming && peerId != null) {
            viewModel.initiateCall(peerId, CallType.VOICE)
        }
    }

    // Handle call ended state with 3-second delay
    LaunchedEffect(uiState) {
        if (uiState is CallUiState.Ended) {
            kotlinx.coroutines.delay(3000) // Wait 3 seconds
            onCallEnded()
        }
    }

    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
    ) {
        when (val state = uiState) {
            is CallUiState.Idle -> {
                IdleContent()
            }
            is CallUiState.Initiating -> {
                InitiatingContent()
            }
            is CallUiState.Ringing -> {
                if (isIncoming) {
                    IncomingCallContent(
                        callerName = state.call.initiator,
                        onAccept = { viewModel.acceptCall() },
                        onReject = { viewModel.rejectCall() }
                    )
                } else {
                    OutgoingCallContent(
                        calleeName = peerId ?: "Unknown",
                        onEndCall = { viewModel.endCall() }
                    )
                }
            }
            is CallUiState.InCall -> {
                InCallContent(
                    callerName = peerId ?: state.session.call.initiator,
                    duration = state.duration,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    participantCount = participants.size,
                    onMuteToggle = { viewModel.toggleMute() },
                    onSpeakerToggle = { viewModel.toggleSpeaker() },
                    onAddPerson = { showContactPicker = true },
                    onEndCall = { viewModel.endCall() }
                )
            }
            is CallUiState.Ended -> {
                EndedContent(
                    duration = state.duration,
                    onClose = onCallEnded
                )
            }
            is CallUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onClose = onCallEnded
                )
            }
        }
    }

    // Contact picker dialog
    if (showContactPicker) {
        ContactPickerDialog(
            contacts = contacts,
            alreadyInCall = participants,
            onContactSelected = { contact ->
                viewModel.addParticipant(contact.id)
                showContactPicker = false
            },
            onDismiss = { showContactPicker = false }
        )
    }
}

@Composable
private fun IdleContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GlassAccent)
    }
}

@Composable
private fun InitiatingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = GlassAccent,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Connecting...",
            style = MaterialTheme.typography.headlineSmall,
            color = GlassText
        )
    }
}

@Composable
private fun IncomingCallContent(
    callerName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    // Pulsing animation for incoming call
    val infiniteTransition = rememberInfiniteTransition(label = "incoming_call_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Caller info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .glass(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Caller",
                    tint = GlassText,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GlassText
            )

            Text(
                text = "Incoming voice call...",
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText.copy(alpha = 0.7f)
            )
        }

        // Call action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Reject button
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = "Reject",
                backgroundColor = VibrantRed,
                onClick = onReject
            )

            // Accept button
            CallActionButton(
                icon = Icons.Default.Call,
                label = "Accept",
                backgroundColor = OnlineGreen,
                onClick = onAccept
            )
        }
    }
}

@Composable
private fun OutgoingCallContent(
    calleeName: String,
    onEndCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Callee info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .glass(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Contact",
                    tint = GlassText,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = calleeName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GlassText
            )

            Text(
                text = "Calling...",
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText.copy(alpha = 0.7f)
            )
        }

        // End call button
        CallActionButton(
            icon = Icons.Default.CallEnd,
            label = "End Call",
            backgroundColor = VibrantRed,
            onClick = onEndCall
        )
    }
}

@Composable
private fun InCallContent(
    callerName: String,
    duration: String,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    participantCount: Int,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onAddPerson: () -> Unit,
    onEndCall: () -> Unit
) {
    // TODO: Get actual connection state from WebRTC
    var isOnline by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Connection status at the top
        ConnectionStatus(isOnline = isOnline)

        // Contact info and duration
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .glass(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Contact",
                    tint = GlassText,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GlassText
            )

            // Participant count badge (if multiple participants)
            if (participantCount > 0) {
                Box(
                    modifier = Modifier
                        .glass()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+$participantCount participant${if (participantCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Duration
            Box(
                modifier = Modifier
                    .glass()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = duration,
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Call controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mute button
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) "Unmute" else "Mute",
                    isActive = isMuted,
                    onClick = onMuteToggle
                )

                // Add person button
                CallControlButton(
                    icon = Icons.Default.PersonAdd,
                    label = "Add",
                    isActive = false,
                    onClick = onAddPerson
                )

                // Speaker button
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = if (isSpeakerOn) "Speaker" else "Earpiece",
                    isActive = isSpeakerOn,
                    onClick = onSpeakerToggle
                )
            }

            // End call button (no label)
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = null,
                backgroundColor = VibrantRed,
                onClick = onEndCall
            )
        }
    }
}

@Composable
private fun EndedContent(
    duration: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "Call Ended",
            tint = GlassText,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Call Ended",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GlassText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Duration: $duration",
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = VibrantRed,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Call Failed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GlassText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassAccent
            )
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String?,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label ?: "Action",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassText
            )
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        GlassAccent.copy(alpha = 0.2f)
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isActive) GlassAccent else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) GlassAccent else GlassText.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) GlassAccent else GlassText.copy(alpha = 0.8f),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ConnectionStatus(isOnline: Boolean) {
    val statusColor = if (isOnline) OnlineGreen else ReconnectingRed
    val statusText = if (isOnline) "Online" else "Reconnecting..."

    // Pulsing animation for reconnecting status
    val infiniteTransition = rememberInfiniteTransition(label = "reconnecting_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .glass()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = if (isOnline) 1f else alpha))
        )

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ContactPickerDialog(
    contacts: List<com.chain.app.domain.model.Contact>,
    alreadyInCall: List<String>,
    onContactSelected: (com.chain.app.domain.model.Contact) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Person to Call",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (contacts.isEmpty()) {
                    Text(
                        text = "No contacts available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    contacts.filter { !alreadyInCall.contains(it.userId) }.forEach { contact ->
                        ContactItem(
                            contact = contact,
                            onClick = { onContactSelected(contact) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = GlassSurface,
        tonalElevation = 8.dp
    )
}

@Composable
private fun ContactItem(
    contact: com.chain.app.domain.model.Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GlassAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Name
            Column {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassText.copy(alpha = 0.6f)
                )
            }
        }

        // Add button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OnlineGreen)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add to call",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
