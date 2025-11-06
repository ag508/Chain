package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Disappearing messages timer dialog
 */
@Composable
fun DisappearingMessagesDialog(
    currentTimer: DisappearingMessageTimer?,
    onTimerSelected: (DisappearingMessageTimer?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = GlassAccent
                )
                Text(
                    text = "Disappearing Messages",
                    color = GlassText
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Set messages to automatically disappear after being read.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Off option
                DisappearingMessageOption(
                    icon = Icons.Default.Close,
                    label = "Off",
                    description = "Messages won't disappear",
                    isSelected = currentTimer == null,
                    onClick = {
                        onTimerSelected(null)
                        onDismiss()
                    }
                )

                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                // Timer options
                DisappearingMessageTimer.entries.forEach { timer ->
                    DisappearingMessageOption(
                        icon = Icons.Default.Timer,
                        label = timer.label,
                        description = timer.description,
                        isSelected = currentTimer == timer,
                        onClick = {
                            onTimerSelected(timer)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = GlassTextSecondary
                )
            }
        },
        containerColor = GlassSurface,
        modifier = modifier
    )
}

@Composable
private fun DisappearingMessageOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) GlassAccent.copy(alpha = 0.1f)
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) GlassAccent else GlassTextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) GlassAccent else GlassText
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = GlassAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Disappearing message timers
 */
enum class DisappearingMessageTimer(
    val label: String,
    val description: String,
    val durationMillis: Long
) {
    SECONDS_5(
        label = "5 seconds",
        description = "Messages disappear 5 seconds after being read",
        durationMillis = 5_000L
    ),
    SECONDS_10(
        label = "10 seconds",
        description = "Messages disappear 10 seconds after being read",
        durationMillis = 10_000L
    ),
    SECONDS_30(
        label = "30 seconds",
        description = "Messages disappear 30 seconds after being read",
        durationMillis = 30_000L
    ),
    MINUTE_1(
        label = "1 minute",
        description = "Messages disappear 1 minute after being read",
        durationMillis = 60_000L
    ),
    MINUTES_5(
        label = "5 minutes",
        description = "Messages disappear 5 minutes after being read",
        durationMillis = 5 * 60_000L
    ),
    MINUTES_30(
        label = "30 minutes",
        description = "Messages disappear 30 minutes after being read",
        durationMillis = 30 * 60_000L
    ),
    HOUR_1(
        label = "1 hour",
        description = "Messages disappear 1 hour after being read",
        durationMillis = 60 * 60_000L
    ),
    HOURS_24(
        label = "24 hours",
        description = "Messages disappear 24 hours after being read",
        durationMillis = 24 * 60 * 60_000L
    ),
    DAYS_7(
        label = "7 days",
        description = "Messages disappear 7 days after being read",
        durationMillis = 7 * 24 * 60 * 60_000L
    )
}

/**
 * Disappearing message indicator badge
 */
@Composable
fun DisappearingMessageBadge(
    timer: DisappearingMessageTimer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassAccent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = timer.label,
            style = MaterialTheme.typography.labelSmall,
            color = GlassAccent
        )
    }
}
