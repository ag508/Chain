package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
import com.chain.app.domain.model.MessageType
import com.chain.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Message bubble component for displaying chat messages
 * Supports sent/received styling, timestamps, status, and reactions
 */
@Composable
fun MessageBubble(
    message: Message,
    isSentByMe: Boolean,
    showSender: Boolean = false,
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isSentByMe) {
        GlassAccent.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val alignment = if (isSentByMe) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Sender name (for group chats, received messages only)
        if (showSender && !isSentByMe) {
            Text(
                text = message.senderId, // TODO: Get actual sender name
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = GlassAccent,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        // Message content
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isSentByMe) 16.dp else 4.dp,
                        topEnd = if (isSentByMe) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(bubbleColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onDoubleTap = { onDoubleTap() }
                    )
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Reply preview (if replying to a message)
                message.replyTo?.let {
                    ReplyPreviewInBubble(
                        replyTo = it, // TODO: Get actual message being replied to
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Message content based on type
                when (message.type) {
                    MessageType.IMAGE -> {
                        ImageMessageContent(
                            imageUri = message.metadata?.get("uri") as? String ?: message.content,
                            caption = message.metadata?.get("caption") as? String,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MessageType.VIDEO -> {
                        VideoMessageContent(
                            videoUri = message.metadata?.get("uri") as? String ?: message.content,
                            caption = message.metadata?.get("caption") as? String,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MessageType.AUDIO -> {
                        AudioMessageContent(
                            audioUri = message.metadata?.get("uri") as? String ?: message.content,
                            duration = message.metadata?.get("duration") as? Long ?: 0L,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MessageType.DOCUMENT -> {
                        DocumentMessageContent(
                            documentUri = message.metadata?.get("uri") as? String,
                            fileName = message.metadata?.get("fileName") as? String ?: message.content,
                            fileSize = message.metadata?.get("fileSize") as? Long ?: 0L,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        // Text message
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Message metadata (time + status)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timestamp
                    Text(
                        text = formatMessageTime(message.timestamp.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    // Status indicator (only for sent messages)
                    if (isSentByMe) {
                        MessageStatusIcon(
                            status = message.status,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Reactions (if any)
        if (message.reactions.isNotEmpty()) {
            MessageReactions(
                reactions = message.reactions,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ReplyPreviewInBubble(
    replyTo: String, // TODO: Should be Message type
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Reply to", // TODO: Show sender name
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GlassAccent,
                fontSize = 10.sp
            )
            Text(
                text = "Message preview...", // TODO: Show actual message content
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun MessageReactions(
    reactions: List<com.chain.app.domain.model.Reaction>,
    modifier: Modifier = Modifier
) {
    // Group reactions by emoji
    val groupedReactions = reactions.groupBy { it.emoji }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        groupedReactions.forEach { (emoji, reactionList) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.bodySmall
                )
                if (reactionList.size > 1) {
                    Text(
                        text = reactionList.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageMessageContent(
    imageUri: String,
    caption: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Image loaded from URI
        AsyncImage(
            model = imageUri,
            contentDescription = "Image message",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // Caption (if any)
        caption?.let {
            if (it.isNotBlank()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun VideoMessageContent(
    videoUri: String,
    caption: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Video thumbnail with play icon overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            // Load video thumbnail from URI
            AsyncImage(
                model = videoUri,
                contentDescription = "Video message",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play icon overlay
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Play video",
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
        }

        // Caption (if any)
        caption?.let {
            if (it.isNotBlank()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AudioMessageContent(
    audioUri: String,
    duration: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button (TODO: implement actual audio playback)
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play audio",
            tint = GlassAccent,
            modifier = Modifier.size(32.dp)
        )

        // Waveform placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassAccent.copy(alpha = 0.1f))
        )

        // Duration
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DocumentMessageContent(
    documentUri: String?,
    fileName: String,
    fileSize: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GlassAccent.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Document icon
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = "Document",
            tint = GlassAccent,
            modifier = Modifier.size(40.dp)
        )

        // File info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (fileSize > 0) {
                Text(
                    text = formatFileSize(fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Download/Open icon (TODO: implement actual file opening)
        Icon(
            imageVector = if (documentUri != null) Icons.Default.OpenInNew else Icons.Default.Download,
            contentDescription = if (documentUri != null) "Open document" else "Download",
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    return if (calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) &&
        calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR)
    ) {
        // Today - show time only
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        // Other days - show date and time
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
