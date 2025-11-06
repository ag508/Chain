package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Message actions bottom sheet
 * Displayed when long-pressing a message
 */
@Composable
fun MessageActionsSheet(
    onReply: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onStar: () -> Unit,
    onInfo: () -> Unit,
    onReact: () -> Unit,
    onDismiss: () -> Unit,
    isMyMessage: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .glassDialog(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Text(
            text = "Message Actions",
            style = MaterialTheme.typography.titleLarge,
            color = GlassText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Quick reactions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val quickReactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
            quickReactions.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            onReact()
                            onDismiss()
                        }
                        .background(GlassAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

        // Action items
        MessageActionItem(
            icon = Icons.Default.Reply,
            label = "Reply",
            onClick = {
                onReply()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.Forward,
            label = "Forward",
            onClick = {
                onForward()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.ContentCopy,
            label = "Copy",
            onClick = {
                onCopy()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.Star,
            label = "Star",
            onClick = {
                onStar()
                onDismiss()
            }
        )

        if (isMyMessage) {
            MessageActionItem(
                icon = Icons.Default.Delete,
                label = "Delete for Everyone",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        } else {
            MessageActionItem(
                icon = Icons.Default.Delete,
                label = "Delete for Me",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        }

        MessageActionItem(
            icon = Icons.Default.Info,
            label = "Message Info",
            onClick = {
                onInfo()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cancel button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cancel",
                color = GlassTextSecondary
            )
        }
    }
}

@Composable
private fun MessageActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = GlassAccent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText
        )
    }
}

/**
 * Delete confirmation dialog
 */
@Composable
fun DeleteMessageDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isMyMessage: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Message?",
                color = GlassText
            )
        },
        text = {
            Text(
                text = if (isMyMessage) {
                    "This message will be deleted for everyone in the chat."
                } else {
                    "This message will be deleted for you only."
                },
                color = GlassTextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = GlassTextSecondary
                )
            }
        },
        containerColor = GlassBackground,
        modifier = modifier
    )
}
