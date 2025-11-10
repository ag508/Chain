package com.chain.app.presentation.chat.detail.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Dialog for selecting contacts to forward messages to
 * Supports multi-select and search
 */
@Composable
fun ForwardContactsDialog(
    contacts: List<ContactItem>,
    onDismiss: () -> Unit,
    onForwardToContacts: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = GlassGradientStart.copy(alpha = 0.95f),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forward to",
                        style = MaterialTheme.typography.titleLarge,
                        color = GlassText
                    )

                    if (selectedContacts.isNotEmpty()) {
                        Text(
                            text = "${selectedContacts.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassAccent
                        )
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search contacts", color = GlassTextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GlassTextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = GlassTextSecondary
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GlassText,
                        unfocusedTextColor = GlassText,
                        focusedBorderColor = GlassAccent,
                        unfocusedBorderColor = GlassTextSecondary.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { contact ->
                    ContactSelectItem(
                        contact = contact,
                        isSelected = selectedContacts.contains(contact.id),
                        onToggleSelect = {
                            selectedContacts = if (selectedContacts.contains(contact.id)) {
                                selectedContacts - contact.id
                            } else {
                                selectedContacts + contact.id
                            }
                        }
                    )
                }

                if (filteredContacts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No contacts available" else "No contacts found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GlassTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedContacts.isNotEmpty()) {
                        onForwardToContacts(selectedContacts.toList())
                        onDismiss()
                    }
                },
                enabled = selectedContacts.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChainSecureGreen,
                    contentColor = Color.White,
                    disabledContainerColor = GlassTextSecondary.copy(alpha = 0.3f),
                    disabledContentColor = GlassTextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Forward")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GlassText)
            }
        }
    )
}

@Composable
private fun ContactSelectItem(
    contact: ContactItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) GlassAccent.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onToggleSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
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
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = GlassText,
                fontWeight = FontWeight.Bold
            )
        }

        // Contact info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )

            if (contact.lastMessage != null) {
                Text(
                    text = contact.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary,
                    maxLines = 1
                )
            }
        }

        // Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            colors = CheckboxDefaults.colors(
                checkedColor = ChainSecureGreen,
                uncheckedColor = GlassTextSecondary,
                checkmarkColor = Color.White
            )
        )
    }
}

/**
 * Data class for contact items in forward dialog
 */
data class ContactItem(
    val id: String,
    val name: String,
    val lastMessage: String? = null,
    val avatarUrl: String? = null
)
