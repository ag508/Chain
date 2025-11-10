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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Action bar shown when messages are selected
 * Provides options like delete, forward, copy, select all
 */
@Composable
fun MessageActionsBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .glass(shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Close button and count
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClearSelection,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selection",
                        tint = GlassText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = GlassText
                )
            }

            // Right side: Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSelectAll,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Select all",
                        tint = GlassText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = GlassText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onForward,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward,
                        contentDescription = "Forward",
                        tint = GlassText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Single message action sheet shown on long press
 */
@Composable
fun SingleMessageActionsSheet(
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onStar: () -> Unit,
    onReact: (String) -> Unit,
    onInfo: () -> Unit = {},
    isSentByMe: Boolean = false,
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
        // Quick reactions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val quickReactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
            quickReactions.forEach { emoji ->
                IconButton(
                    onClick = {
                        onReact(emoji)
                        onDismiss()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = GlassTextSecondary.copy(alpha = 0.2f)
        )

        // Action items
        MessageActionItem(
            icon = Icons.Default.Reply,
            text = "Reply",
            onClick = {
                onReply()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.Forward,
            text = "Forward",
            onClick = {
                onForward()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.ContentCopy,
            text = "Copy",
            onClick = {
                onCopy()
                onDismiss()
            }
        )

        MessageActionItem(
            icon = Icons.Default.Star,
            text = "Star",
            onClick = {
                onStar()
                onDismiss()
            }
        )

        if (isSentByMe) {
            MessageActionItem(
                icon = Icons.Default.Delete,
                text = "Delete",
                onClick = {
                    onDelete()
                    onDismiss()
                },
                isDestructive = true
            )
        }

        MessageActionItem(
            icon = Icons.Default.Info,
            text = "Message Info",
            onClick = {
                onInfo()
                onDismiss()
            }
        )
    }
}

/**
 * Message info dialog showing message details
 */
@Composable
fun MessageInfoDialog(
    message: com.chain.app.domain.model.Message,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Message Info",
                style = MaterialTheme.typography.titleLarge,
                color = GlassText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Type", value = message.type.name)
                InfoRow(
                    label = "Sent",
                    value = java.text.SimpleDateFormat(
                        "MMM dd, yyyy HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(message.timestamp)
                )
                InfoRow(label = "Status", value = message.status.name)
                InfoRow(label = "From", value = message.senderId)

                if (message.reactions.isNotEmpty()) {
                    InfoRow(
                        label = "Reactions",
                        value = message.reactions.groupBy { it.emoji }
                            .map { "${it.key} ${it.value.size}" }
                            .joinToString(", ")
                    )
                }

                if (message.isEncrypted) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ChainSecureGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "End-to-end encrypted",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChainSecureGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = GlassAccent)
            }
        },
        containerColor = GlassGradientStart.copy(alpha = 0.95f),
        modifier = modifier
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GlassTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = GlassText,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun MessageActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassAccent.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else GlassText,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error else GlassText
        )
    }
}
