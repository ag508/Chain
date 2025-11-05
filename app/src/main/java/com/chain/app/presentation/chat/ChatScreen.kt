package com.chain.app.presentation.chat

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    onChatClick: (Chat) -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val zenDots = FontFamily.Default // replace with FontFamily(Font(R.font.zendots_regular))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Chain", color = NeoDarkTextPrimary, fontFamily = zenDots)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = NeoDarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = ChainSecureGreen,
                contentColor = Color.White,
                modifier = Modifier.shadow(4.dp, shape = MaterialTheme.shapes.medium)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        },
        containerColor = NeoDarkSurface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NeoDarkSurface)
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
    }
}

@Composable
private fun ChatList(chats: List<Chat>, onChatClick: (Chat) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        items(chats) { chat ->
            ChatCard(chat = chat, onClick = { onChatClick(chat) })
        }
    }
}

@Composable
private fun ChatCard(chat: Chat, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = NeoDarkSurface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(chat.name, style = MaterialTheme.typography.titleMedium, color = NeoDarkTextPrimary)
            Spacer(Modifier.height(4.dp))
            chat.lastMessage?.let {
                Text(it.content, style = MaterialTheme.typography.bodyMedium, color = NeoDarkTextSecondary, maxLines = 1)
            }
            if (chat.unreadCount > 0) {
                Spacer(Modifier.height(6.dp))
                Text("${chat.unreadCount} unread", style = MaterialTheme.typography.labelSmall, color = ChainSecureGreen)
            }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No chats yet", style = MaterialTheme.typography.headlineSmall, color = NeoDarkTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Start a conversation using the + button", style = MaterialTheme.typography.bodyMedium, color = NeoDarkTextSecondary)
        }
    }
}

@Composable
private fun ErrorChatPlaceholder(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error: $message", color = ChainError, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ChainSecureGreen)) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@Composable
private fun CenterProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ChainSecureGreen)
    }
}
