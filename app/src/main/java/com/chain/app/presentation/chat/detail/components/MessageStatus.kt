package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chain.app.domain.model.MessageStatus

/**
 * Message status indicator showing delivery and read receipts
 *
 * Status indicators:
 * - SENDING: Clock icon (gray)
 * - SENT: Single check mark (gray)
 * - DELIVERED: Double check mark (gray)
 * - READ: Double check mark (blue)
 * - FAILED: Error icon (red)
 */
@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Sending",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = modifier.size(16.dp)
            )
        }

        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = modifier.size(16.dp)
            )
        }

        MessageStatus.DELIVERED -> {
            // Double check mark
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Delivered",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        MessageStatus.READ -> {
            // Double check mark in blue
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read",
                    tint = Color(0xFF34B7F1), // WhatsApp blue
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Failed",
                tint = MaterialTheme.colorScheme.error,
                modifier = modifier.size(16.dp)
            )
        }
    }
}

/**
 * Detailed message status for message info screen
 */
@Composable
fun DetailedMessageStatus(
    status: MessageStatus,
    sentAt: Long? = null,
    deliveredAt: Long? = null,
    readAt: Long? = null,
    modifier: Modifier = Modifier
) {
    // TODO: Implement detailed status view for message info screen
    // Shows full timestamps for sent/delivered/read
}
