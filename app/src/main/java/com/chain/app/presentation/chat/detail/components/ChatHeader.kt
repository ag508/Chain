package com.chain.app.presentation.chat.detail.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chain.app.domain.model.ChatType
import com.chain.app.presentation.components.glass.GlassTextField
import com.chain.app.presentation.theme.*

/**
 * Chat screen header with contact/group info and action buttons
 */
@Composable
fun ChatHeader(
    chatName: String,
    chatType: ChatType,
    onBackClick: () -> Unit,
    onHeaderClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    isOnline: Boolean = false,
    isTyping: Boolean = false,
    lastSeen: Long? = null,
    participantCount: Int? = null,
    onViewProfile: () -> Unit = {},
    onMute: () -> Unit = {},
    onBlock: () -> Unit = {},
    onDisappearingMessages: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Notify parent when search state changes
    LaunchedEffect(showSearchBar) {
        onSearchToggle(showSearchBar)
        if (!showSearchBar) {
            searchQuery = ""
            onSearchQueryChange("")
        }
    }

    // Notify parent when search query changes
    LaunchedEffect(searchQuery) {
        onSearchQueryChange(searchQuery)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(16.dp))
    ) {
        AnimatedContent(
            targetState = showSearchBar,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it }
                ) + fadeIn(
                    animationSpec = tween(300)
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it }
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            },
            label = "search_animation",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { isSearching ->
            if (isSearching) {
                // Search bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            showSearchBar = false
                            searchQuery = ""
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Close search",
                            tint = GlassText
                        )
                    }

                    GlassTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = "Search messages...",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else null,
                        singleLine = true
                    )
                }
            } else {
                // Normal header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassText
                        )
                    }

                    // Chat info (clickable)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onHeaderClick)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with online indicator
                        Box(
                            modifier = Modifier.size(40.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (chatType == ChatType.GROUP) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = chatName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Online indicator (only for direct chats)
                            if (chatType == ChatType.DIRECT && isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }

                        // Chat name and status
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = chatName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GlassText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Status text
                            Text(
                                text = when {
                                    isTyping -> "typing..."
                                    chatType == ChatType.GROUP -> "${participantCount ?: 0} members"
                                    isOnline -> "online"
                                    lastSeen != null -> "last seen ${formatLastSeen(lastSeen)}"
                                    else -> "offline"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isTyping) GlassAccent else GlassTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        // Video call button
                        IconButton(
                            onClick = onVideoCallClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video call",
                                tint = GlassText,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Voice call button
                        IconButton(
                            onClick = onVoiceCallClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice call",
                                tint = GlassText,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // More menu
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = GlassText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            ChatHeaderMenu(
                                expanded = showMenu,
                                onDismiss = { showMenu = false },
                                chatType = chatType,
                                onViewProfile = {
                                    showMenu = false
                                    onViewProfile()
                                },
                                onSearchInChat = {
                                    showMenu = false
                                    showSearchBar = true
                                },
                                onDisappearingMessages = {
                                    showMenu = false
                                    onDisappearingMessages()
                                },
                                onMute = {
                                    showMenu = false
                                    onMute()
                                },
                                onBlock = {
                                    showMenu = false
                                    onBlock()
                                },
                                onViewGroupInfo = {
                                    showMenu = false
                                    onViewProfile()  // Same as view profile for groups
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHeaderMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    chatType: ChatType,
    onViewProfile: () -> Unit,
    onSearchInChat: () -> Unit,
    onMute: () -> Unit,
    onBlock: () -> Unit,
    onViewGroupInfo: () -> Unit,
    onDisappearingMessages: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(200.dp)
            .glass(shape = RoundedCornerShape(12.dp))
    ) {
        // View Profile or Group Info
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (chatType == ChatType.GROUP) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = GlassText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (chatType == ChatType.GROUP) "Group Info" else "View Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText
                    )
                }
            },
            onClick = {
                if (chatType == ChatType.GROUP) onViewGroupInfo() else onViewProfile()
                onDismiss()
            }
        )

        Divider(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = GlassText.copy(alpha = 0.12f)
        )

        // Search in Chat
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GlassText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Search in Chat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText
                    )
                }
            },
            onClick = {
                onSearchInChat()
                onDismiss()
            }
        )

        Divider(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = GlassText.copy(alpha = 0.12f)
        )

        // Disappearing Messages
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GlassText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Disappearing Messages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText
                    )
                }
            },
            onClick = {
                onDisappearingMessages()
                onDismiss()
            }
        )

        Divider(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = GlassText.copy(alpha = 0.12f)
        )

        // Mute
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = GlassText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Mute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText
                    )
                }
            },
            onClick = {
                onMute()
                onDismiss()
            }
        )

        // Block (only for direct chats)
        if (chatType == ChatType.DIRECT) {
            Divider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = GlassText.copy(alpha = 0.12f)
            )

            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Block",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                onClick = {
                    onBlock()
                    onDismiss()
                }
            )
        }
    }
}

private fun formatLastSeen(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 172800_000 -> "yesterday"
        else -> "${diff / 86400_000}d ago"
    }
}
