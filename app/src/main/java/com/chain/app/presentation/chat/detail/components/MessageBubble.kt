package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.app.domain.model.Message
import com.chain.app.domain.model.MessageStatus
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
                message.repliedToMessageId?.let {
                    ReplyPreviewInBubble(
                        replyTo = it, // TODO: Get actual message being replied to
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Message content
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Message metadata (time + status)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edited indicator
                    if (message.isEdited) {
                        Text(
                            text = "edited",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }

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
    reactions: Map<String, List<String>>, // emoji -> list of user IDs
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        reactions.forEach { (emoji, users) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.bodySmall
                )
                if (users.size > 1) {
                    Text(
                        text = users.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
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
