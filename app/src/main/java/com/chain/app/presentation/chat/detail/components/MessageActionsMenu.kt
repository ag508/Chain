package com.chain.app.presentation.chat.detail.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chain.app.domain.model.Message
import com.chain.app.presentation.theme.*

/**
 * Message actions menu shown on long press.
 * Provides options: Reply, Forward, Copy, Delete, React
 */
@Composable
fun MessageActionsDialog(
    message: Message,
    isSentByMe: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .glass()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = "Message Options",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = GlassText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick reactions
            ReactionBar(
                onReact = { emoji ->
                    onReact(emoji)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
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
                    copyToClipboard(context, message.content)
                    onDismiss()
                }
            )

            if (isSentByMe) {
                MessageActionItem(
                    icon = Icons.Default.Delete,
                    text = "Delete",
                    textColor = ChainError,
                    onClick = {
                        onDelete()
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * Quick reaction bar with common emojis.
 */
@Composable
private fun ReactionBar(
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val reactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .glass()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        reactions.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onReact(emoji) }
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Single action item in the menu.
 */
@Composable
private fun MessageActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color = GlassText,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

/**
 * Copy text to clipboard.
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("message", text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Display reactions on a message.
 */
@Composable
fun MessageReactions(
    reactions: Map<String, Int>,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .glass()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reactions.forEach { (emoji, count) ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onReactionClick(emoji) }
                    .background(ChainSecureGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 14.sp
                )
                if (count > 1) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassText,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
