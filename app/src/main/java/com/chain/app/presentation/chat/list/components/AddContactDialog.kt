package com.chain.app.presentation.chat.list.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.components.glass.GlassTextField
import com.chain.app.presentation.theme.*

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAddContact: (phoneNumber: String) -> Unit,
    onCreateGroup: () -> Unit,
    onScanQRCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var phoneNumber by remember { mutableStateOf("") }
    var isPhoneNumberValid by remember { mutableStateOf(false) }

    // Simple phone number validation
    LaunchedEffect(phoneNumber) {
        isPhoneNumberValid = phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .glassDialog(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Add Contact or Group",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GlassText
                )

                // Tab selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) GlassAccent else GlassText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Contact",
                                color = if (selectedTab == 0)
                                    GlassAccent
                                else
                                    GlassText.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }

                    GlassButton(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) GlassAccent else GlassText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Group",
                                color = if (selectedTab == 1)
                                    GlassAccent
                                else
                                    GlassText.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // Add Contact tab
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = "Phone Number",
                                placeholder = "",
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = GlassText.copy(alpha = 0.6f)
                                    )
                                },
                                singleLine = true
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GlassButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", color = GlassText)
                                }

                                GlassButton(
                                    onClick = {
                                        if (isPhoneNumberValid) {
                                            onAddContact(phoneNumber)
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = isPhoneNumberValid
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = GlassText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add", color = GlassText)
                                }
                            }

                            // Alternative options
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = GlassText.copy(alpha = 0.1f)
                            )

                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassText.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            GlassButton(
                                onClick = {
                                    onDismiss()
                                    onScanQRCode()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = GlassText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan QR Code", color = GlassText)
                            }
                        }
                    }

                    1 -> {
                        // Create Group tab
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GlassAccent.copy(alpha = 0.6f)
                            )

                            Text(
                                text = "Create a new group",
                                style = MaterialTheme.typography.titleMedium,
                                color = GlassText,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Add members, set group name, and start chatting",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassText.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GlassButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", color = GlassText)
                                }

                                GlassButton(
                                    onClick = {
                                        onCreateGroup()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = GlassText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create", color = GlassText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
