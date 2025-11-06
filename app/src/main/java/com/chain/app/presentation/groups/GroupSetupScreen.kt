package com.chain.app.presentation.groups

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.components.glass.GlassCard
import com.chain.app.presentation.components.glass.GlassTextField
import com.chain.app.presentation.theme.*

/**
 * Screen for setting up group details
 * Step 2: Set group name, icon, and finalize creation
 */
@Composable
fun GroupSetupScreen(
    selectedContactIds: List<String>,
    onBackClick: () -> Unit,
    onCreateGroup: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupSetupViewModel = hiltViewModel()
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    val isValid = groupName.isNotBlank() && groupName.length >= 3

    // Background gradient
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Text(
                        text = "Group Setup",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GlassText,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${selectedContactIds.size} members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group icon
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Group Icon",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                            .clickable { showIconPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedIcon != null) {
                            // TODO: Show actual icon/image
                            Text(
                                text = selectedIcon!!,
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    modifier = Modifier.size(40.dp),
                                    tint = GlassAccent
                                )
                                Text(
                                    text = "Add Icon",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassTextSecondary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tap to select an icon or photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )
                }
            }

            // Group name
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Group Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )

                    GlassTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Enter group name...",
                        singleLine = true
                    )

                    if (groupName.isNotBlank() && groupName.length < 3) {
                        Text(
                            text = "Group name must be at least 3 characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Group description (optional)
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Description (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )

                    GlassTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = "Add a description...",
                        maxLines = 3
                    )
                }
            }

            // Group settings
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Group Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText
                    )

                    var allowMembersToAddOthers by remember { mutableStateOf(false) }
                    var allowMembersToEditInfo by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow members to add others",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassText
                            )
                            Text(
                                text = "Let anyone add new members",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextSecondary
                            )
                        }
                        Switch(
                            checked = allowMembersToAddOthers,
                            onCheckedChange = { allowMembersToAddOthers = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlassAccent,
                                checkedTrackColor = GlassAccent.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Allow members to edit info",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassText
                            )
                            Text(
                                text = "Let anyone change group name and icon",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextSecondary
                            )
                        }
                        Switch(
                            checked = allowMembersToEditInfo,
                            onCheckedChange = { allowMembersToEditInfo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlassAccent,
                                checkedTrackColor = GlassAccent.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for button
        }

        // Create button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            GlassButton(
                onClick = {
                    viewModel.createGroup(
                        groupName = groupName,
                        selectedContactIds = selectedContactIds,
                        groupIcon = selectedIcon,
                        onSuccess = { groupChat ->
                            // Call the original callback for navigation
                            onCreateGroup(groupName, selectedIcon)
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid && uiState !is GroupSetupUiState.Loading
            ) {
                if (uiState is GroupSetupUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GlassBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState is GroupSetupUiState.Loading) "Creating..." else "Create Group")
            }
        }
        }

        // Show error message if any (outside Column, inside outer Box)
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                errorMessage = null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = RoundedCornerShape(16.dp)),
                    color = ChainError.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassText,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = GlassText
                            )
                        }
                    }
                }
            }
        }

        // Icon picker dialog
        if (showIconPicker) {
            IconPickerDialog(
                onDismiss = { showIconPicker = false },
                onIconSelected = { icon ->
                    selectedIcon = icon
                    showIconPicker = false
                }
            )
        }
    }
}

@Composable
private fun IconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Group Icon")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose an emoji or icon for your group",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Simple emoji grid
                val emojis = listOf(
                    "👥", "👨‍👩‍👧‍👦", "🎉", "💼", "🏫",
                    "⚽", "🎮", "📚", "🍕", "☕",
                    "🌍", "🎵", "🎨", "💪", "🚀"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.chunked(5).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.displaySmall,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.3f
                                            )
                                        )
                                        .clickable { onIconSelected(emoji) }
                                        .wrapContentSize(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
