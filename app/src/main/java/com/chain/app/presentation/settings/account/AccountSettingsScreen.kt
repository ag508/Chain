package com.chain.app.presentation.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    onPrivacyClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val accountInfo by viewModel.accountInfo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account",
                        color = GlassText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.glass()
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GlassGradientStart, GlassGradientEnd)
                )
            )
    ) { paddingValues ->
        when (uiState) {
            is AccountSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GlassAccent)
                }
            }
            is AccountSettingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = (uiState as AccountSettingsUiState.Error).message,
                            color = GlassText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.refreshAccountInfo() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GlassAccent
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                accountInfo?.let { info ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Profile Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(GlassAccent.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = info.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = GlassText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = info.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GlassText,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = info.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlassTextSecondary
                                )
                            }
                        }

                        // Account Information
                        Text(
                            text = "Account Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = GlassAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AccountInfoItem(
                                    icon = Icons.Default.Person,
                                    title = "Name",
                                    value = info.displayName,
                                    onClick = { showNameDialog = true }
                                )

                                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                                AccountInfoItem(
                                    icon = Icons.Default.Info,
                                    title = "About",
                                    value = info.about.ifBlank { "No status set" },
                                    onClick = { showAboutDialog = true }
                                )

                                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                                AccountInfoItem(
                                    icon = Icons.Default.Phone,
                                    title = "Phone",
                                    value = info.phoneNumber,
                                    onClick = null
                                )

                                if (info.email.isNotBlank()) {
                                    Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                                    AccountInfoItem(
                                        icon = Icons.Default.Email,
                                        title = "Email",
                                        value = info.email,
                                        onClick = null
                                    )
                                }

                                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                                AccountInfoItem(
                                    icon = Icons.Default.Badge,
                                    title = "User ID",
                                    value = info.userId.take(16) + "...",
                                    onClick = null
                                )
                            }
                        }

                        // Settings Links
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = GlassAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsLinkItem(
                                    icon = Icons.Default.Lock,
                                    title = "Privacy",
                                    subtitle = "Blocked contacts, read receipts",
                                    onClick = onPrivacyClick
                                )

                                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                                SettingsLinkItem(
                                    icon = Icons.Default.Security,
                                    title = "Security",
                                    subtitle = "Biometric lock, encryption keys",
                                    onClick = onSecurityClick
                                )
                            }
                        }

                        // Delete Account
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Account",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showNameDialog) {
        EditTextDialog(
            title = "Edit Name",
            currentValue = accountInfo?.displayName ?: "",
            onDismiss = { showNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateDisplayName(newName)
                showNameDialog = false
            }
        )
    }

    // Edit About Dialog
    if (showAboutDialog) {
        EditTextDialog(
            title = "Edit About",
            currentValue = accountInfo?.about ?: "",
            onDismiss = { showAboutDialog = false },
            onConfirm = { newAbout ->
                viewModel.updateAbout(newAbout)
                showAboutDialog = false
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account?",
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassText
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone. All your messages, contacts, and data will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Implement delete account
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = GlassText)
                }
            },
            containerColor = GlassSurface
        )
    }
}

@Composable
private fun AccountInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = GlassTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsLinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = GlassTextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GlassText
            )
        },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GlassText,
                    unfocusedTextColor = GlassText,
                    focusedBorderColor = GlassAccent,
                    unfocusedBorderColor = GlassTextSecondary.copy(alpha = 0.5f)
                ),
                singleLine = title == "Edit Name"
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(textValue) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChainSecureGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GlassText)
            }
        },
        containerColor = GlassSurface
    )
}
