package com.chain.app.presentation.chat.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.components.glass.GlassTextField
import com.chain.app.presentation.theme.*

/**
 * Message input bar for composing and sending messages
 * Includes text input, emoji picker, attachments, voice recording, and send button
 */
@Composable
fun MessageInputBar(
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    onEmojiClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onSendAudio: (android.net.Uri, Long) -> Unit,
    hasMicrophonePermission: Boolean,
    onRequestMicrophonePermission: () -> Unit,
    replyingTo: String? = null,
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasText = text.text.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Reply banner
        if (replyingTo != null) {
            ReplyBanner(
                replyTo = replyingTo,
                onCancel = onCancelReply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Text input field
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji button
                    IconButton(
                        onClick = onEmojiClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = GlassText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Text field
                    GlassTextField(
                        value = text.text,
                        onValueChange = { onTextChange(TextFieldValue(it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = "Message...",
                        maxLines = 5
                    )

                    // Attachment button (shown when no text)
                    if (!hasText) {
                        IconButton(
                            onClick = onAttachmentClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = GlassText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Send button or Voice record button
            AnimatedVisibility(
                visible = hasText,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                // Send button - Improved appearance and functionality
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ChainSecureGreen)
                        .clickable { onSendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (!hasText) {
                // Voice record button
                VoiceRecordButton(
                    onRecordingComplete = onSendAudio,
                    hasPermission = hasMicrophonePermission,
                    onRequestPermission = onRequestMicrophonePermission
                )
            }
        }
    }
}

@Composable
private fun ReplyBanner(
    replyTo: String, // TODO: Should be Message type
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassAccent.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = GlassAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Replying to", // TODO: Show sender name
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassAccent
                    )
                }

                Text(
                    text = "Message preview...", // TODO: Show actual message content
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary,
                    maxLines = 2
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel reply",
                    tint = GlassTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
/**
 * Attachment menu showing different attachment types
 */
@Composable
fun AttachmentMenu(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onLocationClick: () -> Unit,
    onContactClick: () -> Unit,
    onPollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassDialog(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // More opaque glass effect
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Send Attachment",
                style = MaterialTheme.typography.titleLarge,
                color = GlassText
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = GlassTextSecondary
                )
            }
        }

        // Attachment options in grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    color = ChainSecureGreen,
                    onClick = {
                        onDismiss()
                        onCameraClick()
                    }
                )

                AttachmentOption(
                    icon = Icons.Default.Photo,
                    label = "Gallery",
                    color = GlassAccent,
                    onClick = {
                        onDismiss()
                        onGalleryClick()
                    }
                )

                AttachmentOption(
                    icon = Icons.Default.InsertDriveFile,
                    label = "Document",
                    color = ChainSecureBlue,
                    onClick = {
                        onDismiss()
                        onDocumentClick()
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentOption(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = ChainSecureGreen,
                    onClick = {
                        onDismiss()
                        onLocationClick()
                    }
                )

                AttachmentOption(
                    icon = Icons.Default.Person,
                    label = "Contact",
                    color = GlassAccent,
                    onClick = {
                        onDismiss()
                        onContactClick()
                    }
                )

                AttachmentOption(
                    icon = Icons.Default.Poll,
                    label = "Poll",
                    color = ChainSecureBlue,
                    onClick = {
                        onDismiss()
                        onPollClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .glass()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GlassText,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
