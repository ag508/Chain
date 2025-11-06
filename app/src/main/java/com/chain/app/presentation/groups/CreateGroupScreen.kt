package com.chain.app.presentation.groups

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
import com.chain.app.presentation.chat.list.components.SearchBar
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.components.glass.GlassCard
import com.chain.app.presentation.theme.*

/**
 * Screen for creating a new group chat
 * Step 1: Select contacts to add to the group
 */
@Composable
fun CreateGroupScreen(
    onBackClick: () -> Unit,
    onNextClick: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }

    // TODO: Get actual contacts from repository
    val contacts = remember {
        listOf(
            GroupContact("1", "Alice Johnson", "+1234567890", null),
            GroupContact("2", "Bob Smith", "+9876543210", null),
            GroupContact("3", "Carol White", "+5555555555", null),
            GroupContact("4", "David Brown", "+4444444444", null),
            GroupContact("5", "Eve Davis", "+3333333333", null)
        )
    }

    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Background gradient
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Group",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GlassText
                        )
                        if (selectedContacts.isNotEmpty()) {
                            Text(
                                text = "${selectedContacts.size} selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { },
                placeholder = "Search contacts..."
            )
        }

        // Selected contacts chips (if any)
        if (selectedContacts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                GlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Selected (${selectedContacts.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = GlassTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedContacts.take(5).forEach { contactId ->
                                val contact = contacts.find { it.id == contactId }
                                contact?.let {
                                    SelectedContactChip(
                                        name = it.name,
                                        onRemove = {
                                            selectedContacts = selectedContacts - contactId
                                        }
                                    )
                                }
                            }
                            if (selectedContacts.size > 5) {
                                Text(
                                    text = "+${selectedContacts.size - 5}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlassTextSecondary,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contacts list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredContacts) { contact ->
                ContactSelectItem(
                    contact = contact,
                    isSelected = selectedContacts.contains(contact.id),
                    onToggle = {
                        selectedContacts = if (selectedContacts.contains(contact.id)) {
                            selectedContacts - contact.id
                        } else {
                            selectedContacts + contact.id
                        }
                    }
                )
            }
        }

        // Next button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            GlassButton(
                onClick = { onNextClick(selectedContacts.toList()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedContacts.isNotEmpty()
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactSelectItem(
    contact: GroupContact,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GlassAccent,
                    uncheckedColor = GlassTextSecondary
                )
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Contact info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SelectedContactChip(
    name: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassAccent.copy(alpha = 0.2f),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name.split(" ").firstOrNull() ?: name,
                style = MaterialTheme.typography.bodySmall,
                color = GlassText
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove),
                tint = GlassText
            )
        }
    }
}

/**
 * Contact data for group creation
 */
data class GroupContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val avatar: String?
)
