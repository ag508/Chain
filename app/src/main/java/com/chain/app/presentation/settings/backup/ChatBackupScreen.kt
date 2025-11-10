package com.chain.app.presentation.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBackupScreen(
    onBackClick: () -> Unit,
    viewModel: ChatBackupViewModel = hiltViewModel()
) {
    val backupSettings by viewModel.backupSettings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showFrequencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat Backup",
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
            .gradientBackground()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Last Backup Info Card
            BackupInfoCard(
                lastBackupDate = backupSettings.lastBackupDate,
                uiState = uiState,
                onBackupNow = { viewModel.backupNow() }
            )

            // Auto Backup Settings
            SettingsSection(title = "Auto Backup") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsToggleItem(
                        icon = Icons.Default.Backup,
                        title = "Auto Backup",
                        description = "Automatically backup chats",
                        checked = backupSettings.autoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                    )

                    if (backupSettings.autoBackupEnabled) {
                        SettingsClickableItem(
                            icon = Icons.Default.Schedule,
                            title = "Backup Frequency",
                            description = backupSettings.backupFrequency.replaceFirstChar { it.uppercase() },
                            onClick = { showFrequencyDialog = true }
                        )
                    }
                }
            }

            // Backup Options
            SettingsSection(title = "Backup Options") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsToggleItem(
                        icon = Icons.Default.VideoLibrary,
                        title = "Include Videos",
                        description = "Include videos in backup (increases size)",
                        checked = backupSettings.includeVideos,
                        onCheckedChange = { viewModel.setIncludeVideos(it) }
                    )

                    SettingsToggleItem(
                        icon = Icons.Default.MobileData,
                        title = "Backup over Cellular",
                        description = "Allow backup using mobile data",
                        checked = backupSettings.backupOverCellular,
                        onCheckedChange = { viewModel.setBackupOverCellular(it) }
                    )
                }
            }

            // Backup Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GlassAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "About Backups",
                            style = MaterialTheme.typography.titleSmall,
                            color = GlassText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• Backups are stored locally on your device\n" +
                                "• End-to-end encrypted messages remain encrypted\n" +
                                "• Backups include messages, photos, and documents\n" +
                                "• Videos can be optionally included",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.5
                    )
                }
            }
        }
    }

    // Frequency Selection Dialog
    if (showFrequencyDialog) {
        FrequencySelectionDialog(
            currentFrequency = backupSettings.backupFrequency,
            onDismiss = { showFrequencyDialog = false },
            onFrequencySelected = { frequency ->
                viewModel.setBackupFrequency(frequency)
                showFrequencyDialog = false
            }
        )
    }
}

@Composable
private fun BackupInfoCard(
    lastBackupDate: Long?,
    uiState: BackupUiState,
    onBackupNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Last Backup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )

                    Text(
                        text = if (lastBackupDate != null) {
                            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            formatter.format(Date(lastBackupDate))
                        } else {
                            "Never"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (lastBackupDate != null) ChainSecureGreen else GlassTextSecondary,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Status message
            when (uiState) {
                is BackupUiState.Backing -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GlassAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Backing up...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassAccent
                        )
                    }
                }
                is BackupUiState.Success -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ChainSecureGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ChainSecureGreen
                        )
                    }
                }
                is BackupUiState.Error -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }

            // Backup Now Button
            Button(
                onClick = onBackupNow,
                enabled = uiState !is BackupUiState.Backing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChainSecureGreen,
                    contentColor = Color.White,
                    disabledContainerColor = GlassTextSecondary.copy(alpha = 0.3f),
                    disabledContentColor = GlassTextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Backup Now",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = GlassAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
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
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChainSecureGreen,
                uncheckedThumbColor = GlassTextSecondary,
                uncheckedTrackColor = GlassTextSecondary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
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
                text = description,
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
private fun FrequencySelectionDialog(
    currentFrequency: String,
    onDismiss: () -> Unit,
    onFrequencySelected: (String) -> Unit
) {
    val frequencies = listOf("daily", "weekly", "monthly")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Backup Frequency",
                style = MaterialTheme.typography.titleLarge,
                color = GlassText
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frequencies.forEach { frequency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (frequency == currentFrequency) GlassAccent.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { onFrequencySelected(frequency) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = frequency.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            color = GlassText,
                            fontWeight = if (frequency == currentFrequency) FontWeight.Bold else FontWeight.Normal
                        )

                        if (frequency == currentFrequency) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ChainSecureGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = GlassAccent)
            }
        },
        containerColor = GlassGradientStart.copy(alpha = 0.95f)
    )
}
