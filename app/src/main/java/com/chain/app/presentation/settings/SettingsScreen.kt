package com.chain.app.presentation.settings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccountSettingsClick: () -> Unit = {},
    onPrivacySettingsClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GlassText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account section
                SettingsSection(title = "Account") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Account",
                        subtitle = "Privacy, security, change number",
                        onClick = onAccountSettingsClick
                    )
                }

                // Privacy section
                SettingsSection(title = "Privacy") {
                    Column {
                        SettingsSwitchItem(
                            icon = Icons.Default.Visibility,
                            title = "Read Receipts",
                            subtitle = "If turned off, you won't send or receive read receipts",
                            checked = settings.readReceipts,
                            onCheckedChange = { viewModel.setReadReceipts(it) }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsSwitchItem(
                            icon = Icons.Default.Circle,
                            title = "Online Status",
                            subtitle = "Show when you're online",
                            checked = settings.onlineStatus,
                            onCheckedChange = { viewModel.setOnlineStatus(it) }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.Block,
                            title = "Blocked Contacts",
                            subtitle = "0 blocked",
                            onClick = { /* TODO: Navigate to blocked contacts */ }
                        )
                    }
                }

                // Notifications section
                SettingsSection(title = "Notifications") {
                    Column {
                        SettingsSwitchItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Enable app notifications",
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsSwitchItem(
                            icon = Icons.Default.VolumeUp,
                            title = "Sound",
                            subtitle = "Play sounds for incoming messages",
                            checked = settings.soundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsSwitchItem(
                            icon = Icons.Default.Vibration,
                            title = "Vibration",
                            subtitle = "Vibrate for incoming messages",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )
                    }
                }

                // Security section
                SettingsSection(title = "Security") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Unlock Chain with fingerprint or face",
                        checked = settings.biometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) }
                    )
                }

                // Appearance section
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = when (settings.themeMode) {
                            "light" -> "Light"
                            "dark" -> "Dark"
                            else -> "System default"
                        },
                        onClick = { /* TODO: Show theme selector */ }
                    )
                }

                // Chats section
                SettingsSection(title = "Chats") {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Backup,
                            title = "Chat Backup",
                            subtitle = "Backup and restore your chats",
                            onClick = { /* TODO: Navigate to backup settings */ }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.Wallpaper,
                            title = "Wallpaper",
                            subtitle = "Change chat background",
                            onClick = { /* TODO: Navigate to wallpaper selector */ }
                        )
                    }
                }

                // Data section
                SettingsSection(title = "Data and Storage") {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.NetworkCheck,
                            title = "Network Usage",
                            subtitle = "View data usage statistics",
                            onClick = { /* TODO: Navigate to network usage */ }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.Download,
                            title = "Auto-Download Media",
                            subtitle = "Configure automatic media downloads",
                            onClick = { /* TODO: Navigate to auto-download settings */ }
                        )
                    }
                }

                // Help section
                SettingsSection(title = "Help") {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Help,
                            title = "FAQ",
                            subtitle = "Frequently asked questions",
                            onClick = { /* TODO: Navigate to FAQ */ }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.ContactSupport,
                            title = "Contact Support",
                            subtitle = "Get help from our team",
                            onClick = { /* TODO: Navigate to support */ }
                        )
                    }
                }

                // About section
                SettingsSection(title = "About") {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            subtitle = "1.0.0 (Beta)",
                            onClick = { /* Nothing */ }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.Description,
                            title = "Terms of Service",
                            subtitle = "Read our terms",
                            onClick = { /* TODO: Open terms */ }
                        )
                        Divider(
                            color = GlassBorder.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 56.dp)
                        )
                        SettingsItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Read our privacy policy",
                            onClick = { /* TODO: Open privacy policy */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = GlassAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass()
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GlassText.copy(alpha = 0.7f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = GlassText.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GlassText.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GlassAccent,
                checkedTrackColor = GlassAccent.copy(alpha = 0.5f),
                uncheckedThumbColor = GlassText.copy(alpha = 0.5f),
                uncheckedTrackColor = GlassText.copy(alpha = 0.2f)
            )
        )
    }
}
