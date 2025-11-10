package com.chain.app.presentation.settings.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.chain.app.presentation.theme.*

@Composable
fun BlockedContactsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BlockedContactsViewModel = hiltViewModel()
) {
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showUnblockDialog by remember { mutableStateOf<BlockedUser?>(null) }

    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .glassIconButton()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GlassText
                    )
                }

                Text(
                    text = "Blocked Contacts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GlassText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            when (uiState) {
                is BlockedContactsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GlassAccent)
                    }
                }
                is BlockedContactsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as BlockedContactsUiState.Error).message,
                            color = ChainError,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is BlockedContactsUiState.Success -> {
                    if (blockedUsers.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = GlassTextSecondary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "No Blocked Contacts",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GlassText
                                )
                                Text(
                                    text = "Users you block will appear here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlassTextSecondary
                                )
                            }
                        }
                    } else {
                        // List of blocked users
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(blockedUsers) { user ->
                                BlockedContactItem(
                                    user = user,
                                    onUnblockClick = { showUnblockDialog = user }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Unblock confirmation dialog
        showUnblockDialog?.let { user ->
            UnblockDialog(
                userName = user.name,
                onDismiss = { showUnblockDialog = null },
                onConfirm = {
                    viewModel.unblockUser(user.id)
                    showUnblockDialog = null
                }
            )
        }
    }
}

@Composable
private fun BlockedContactItem(
    user: BlockedUser,
    onUnblockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassAccent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = GlassText,
                    fontWeight = FontWeight.Bold
                )
            }

            // User info
            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Blocked on ${user.blockedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary
                )
            }
        }

        // Unblock button
        TextButton(
            onClick = onUnblockClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = ChainError
            )
        ) {
            Text("Unblock")
        }
    }
}

@Composable
private fun UnblockDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = GlassAccent,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Unblock $userName?",
                style = MaterialTheme.typography.titleLarge,
                color = GlassText
            )
        },
        text = {
            Text(
                text = "$userName will be able to send you messages and call you again.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ChainSecureGreen
                )
            ) {
                Text("Unblock")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GlassText
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = GlassSurface,
        textContentColor = GlassText
    )
}

data class BlockedUser(
    val id: String,
    val name: String,
    val blockedDate: String
)
