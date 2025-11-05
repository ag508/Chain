package com.chain.app.presentation.chat

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.domain.model.Chat
import com.chain.app.presentation.theme.*

/**
 * Simple wrapper for the chat list screen.
 */
@Composable
fun ChatScreen() {
    ChatListScreen()
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    onChatClick: (Chat) -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf("chats") }

    // Background gradient
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Glassmorphic header with rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Header left - Chain logo text in Zen Dots
                    Text(
                        text = "Chain",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize
                        ),
                        color = GlassText
                    )

                    // Header right
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .glassIconButton()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GlassText
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .glassIconButton()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = GlassText
                            )
                        }
                    }
                }
                }
            }

            // Chat content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Crossfade(targetState = uiState, label = "chat_state") { state ->
                    when (state) {
                        is ChatListUiState.Loading -> CenterProgress()
                        is ChatListUiState.Success -> {
                            if (state.chats.isEmpty()) EmptyChatPlaceholder() else ChatList(state.chats, onChatClick)
                        }
                        is ChatListUiState.Error -> ErrorChatPlaceholder(state.message, onRetry = viewModel::refresh)
                    }
                }
            }

            // Glassmorphic bottom nav with rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Chats tab
                    BottomNavItem(
                        icon = Icons.Default.Chat,
                        label = "Chats",
                        isActive = currentTab == "chats",
                        onClick = { currentTab = "chats" }
                    )

                    // Calls tab
                    BottomNavItem(
                        icon = Icons.Default.Phone,
                        label = "Calls",
                        isActive = currentTab == "calls",
                        onClick = { currentTab = "calls" }
                    )
                }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                color = if (isActive) GlassCardBg else GlassCardBg.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) GlassText else GlassTextSecondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isActive) GlassText else GlassTextSecondary
        )
    }
}

@Composable
private fun ChatList(chats: List<Chat>, onChatClick: (Chat) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ChatItem(chat = chat, onClick = { onChatClick(chat) })
        }
    }
}

@Composable
private fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar: 52x52dp with 16dp border radius, glass effect
        Box(
            modifier = Modifier
                .size(52.dp)
                .glassAvatar(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GlassText
            )
        }

        // Chat content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = GlassText
                )
                Text(
                    text = "2:45 PM", // TODO: Get real time
                    style = MaterialTheme.typography.labelMedium,
                    color = GlassTextMuted
                )
            }

            Spacer(Modifier.height(6.dp))

            chat.lastMessage?.let {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        modifier = Modifier.size(16.dp),
                        tint = GlassTextSecondary
                    )
                    Text(
                        text = it.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Unread badge
        if (chat.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(GlassAccent, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassBg
                )
            }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No chats yet", style = MaterialTheme.typography.headlineSmall, color = GlassText)
            Spacer(Modifier.height(8.dp))
            Text("Start a conversation using the + button", style = MaterialTheme.typography.bodyMedium, color = GlassTextSecondary)
        }
    }
}

@Composable
private fun ErrorChatPlaceholder(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error: $message", color = ChainError, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = GlassAccent)) {
                Text("Retry", color = GlassBg)
            }
        }
    }
}

@Composable
private fun CenterProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = GlassAccent)
    }
}
