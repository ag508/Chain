package com.chain.app.presentation.contacts

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
import com.chain.app.presentation.chat.list.components.SearchBar
import com.chain.app.presentation.components.glass.GlassButton
import com.chain.app.presentation.components.glass.GlassCard
import com.chain.app.presentation.theme.*

/**
 * Contact search screen for finding and adding contacts by phone number
 */
@Composable
fun ContactSearchScreen(
    onBackClick: () -> Unit,
    onContactSelected: (String) -> Unit,
    onQRCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

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
        // Header with back button
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
                        text = "Add Contact",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GlassText,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onQRCodeClick,
                        modifier = Modifier
                            .size(40.dp)
                            .glassIconButton()
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = GlassText
                        )
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
                onQueryChange = { query ->
                    viewModel.searchContacts(query)
                },
                onSearch = { viewModel.searchContacts(searchQuery) },
                placeholder = "Search by phone number..."
            )
        }

        // Search results
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GlassAccent)
                    }
                }

                searchQuery.isEmpty() -> {
                    EmptySearchState()
                }

                searchResults.isEmpty() && searchQuery.length >= 3 -> {
                    NoResultsState(searchQuery)
                }

                else -> {
                    ContactSearchResults(
                        results = searchResults,
                        onContactClick = { phoneNumber ->
                            // Add contact and then navigate
                            viewModel.addContact(
                                phoneNumber = phoneNumber,
                                onSuccess = { contact ->
                                    onContactSelected(phoneNumber)
                                },
                                onError = { error ->
                                    // TODO: Show error message
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonSearch,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GlassTextSecondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search for Contacts",
            style = MaterialTheme.typography.titleLarge,
            color = GlassText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a phone number to find contacts",
            style = MaterialTheme.typography.bodyMedium,
            color = GlassTextSecondary
        )
    }
}

@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GlassTextSecondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Contacts Found",
            style = MaterialTheme.typography.titleLarge,
            color = GlassText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No contacts found for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = GlassTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlassButton(
            onClick = { /* TODO: Invite contact */ }
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Invite to Chain")
        }
    }
}

@Composable
private fun ContactSearchResults(
    results: List<ContactSearchResult>,
    onContactClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(results) { contact ->
            ContactResultItem(
                contact = contact,
                onClick = { onContactClick(contact.phoneNumber) }
            )
        }
    }
}

@Composable
private fun ContactResultItem(
    contact: ContactSearchResult,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (contact.avatar != null) {
                    // TODO: Load actual avatar
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = contact.name?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Contact info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Add button or added indicator
            if (contact.isAdded) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Added",
                    tint = GlassAccent,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add contact",
                    tint = GlassAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Contact search result data class
 */
data class ContactSearchResult(
    val phoneNumber: String,
    val name: String?,
    val avatar: String?,
    val isAdded: Boolean
)
