package com.chain.app.presentation.call

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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

    // Initiate call if outgoing
    LaunchedEffect(peerId, isIncoming) {
        if (!isIncoming && peerId != null) {
            viewModel.initiateCall(peerId, CallType.VOICE)
        }
    }

    // Handle call ended state
    LaunchedEffect(uiState) {
        if (uiState is CallUiState.Ended) {
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
                    onMuteToggle = { viewModel.toggleMute() },
                    onSpeakerToggle = { viewModel.toggleSpeaker() },
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
                backgroundColor = MaterialTheme.colorScheme.error,
                onClick = onReject
            )

            // Accept button
            CallActionButton(
                icon = Icons.Default.Call,
                label = "Accept",
                backgroundColor = Color(0xFF34C759), // Green
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
            backgroundColor = MaterialTheme.colorScheme.error,
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
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
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

                // Speaker button
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = if (isSpeakerOn) "Speaker" else "Earpiece",
                    isActive = isSpeakerOn,
                    onClick = onSpeakerToggle
                )
            }

            // End call button
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = "End Call",
                backgroundColor = MaterialTheme.colorScheme.error,
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
            tint = MaterialTheme.colorScheme.error,
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
    label: String,
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
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GlassText
        )
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
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .glass()
                .then(
                    if (isActive) {
                        Modifier.background(
                            GlassAccent.copy(alpha = 0.3f),
                            CircleShape
                        )
                    } else Modifier
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) GlassAccent else GlassText,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GlassText
        )
    }
}
