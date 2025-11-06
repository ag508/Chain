package com.chain.app.presentation.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.domain.model.Call
import com.chain.app.domain.model.CallStatus
import com.chain.app.domain.model.CallType
import com.chain.app.domain.model.Contact
import com.chain.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallTabScreen(
    onVoiceCallClick: (String) -> Unit,
    onVideoCallClick: (String) -> Unit,
    viewModel: CallTabViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val callHistory by viewModel.callHistory.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Search bar for contacts
        CallSearchBar(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSearchToggle = { viewModel.toggleSearch() },
            isSearching = isSearching,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Show filtered contacts when searching, otherwise show call history
        if (isSearching && searchQuery.isNotEmpty()) {
            ContactSearchResults(
                contacts = contacts.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery, ignoreCase = true)
                },
                onVoiceCallClick = {
                    onVoiceCallClick(it.userId)
                    viewModel.toggleSearch()
                    viewModel.updateSearchQuery("")
                },
                onVideoCallClick = {
                    onVideoCallClick(it.userId)
                    viewModel.toggleSearch()
                    viewModel.updateSearchQuery("")
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Call history list
            if (callHistory.isEmpty()) {
                EmptyCallHistory(modifier = Modifier.weight(1f))
            } else {
                CallHistoryList(
                    calls = callHistory,
                    onCallClick = onVoiceCallClick,
                    onVideoCallClick = onVideoCallClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CallSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )

        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Search contacts to call...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassText.copy(alpha = 0.5f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = GlassText,
                unfocusedTextColor = GlassText
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = GlassText.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactSearchResults(
    contacts: List<Contact>,
    onVoiceCallClick: (Contact) -> Unit,
    onVideoCallClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No contacts found",
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contacts) { contact ->
                ContactSearchItem(
                    contact = contact,
                    onVoiceCallClick = { onVoiceCallClick(contact) },
                    onVideoCallClick = { onVideoCallClick(contact) }
                )
            }
        }
    }
}

@Composable
private fun ContactSearchItem(
    contact: Contact,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
                    .background(GlassAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GlassAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = GlassText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassText.copy(alpha = 0.6f)
                )
            }
        }

        // Call action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice call button
            IconButton(
                onClick = onVoiceCallClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF34C759).copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Voice call",
                    tint = Color(0xFF34C759),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Video call button
            IconButton(
                onClick = onVideoCallClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassAccent.copy(alpha = 0.2f))
                    .border(1.dp, GlassAccent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video call",
                    tint = GlassAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CallHistoryList(
    calls: List<Call>,
    onCallClick: (String) -> Unit,
    onVideoCallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(calls) { call ->
            CallHistoryItem(
                call = call,
                onCallClick = {
                    if (call.type == CallType.VIDEO) {
                        onVideoCallClick(call.initiator)
                    } else {
                        onCallClick(call.initiator)
                    }
                }
            )
        }
    }
}

@Composable
private fun CallHistoryItem(
    call: Call,
    onCallClick: () -> Unit
) {
    val isIncoming = call.status == CallStatus.ANSWERED || call.status == CallStatus.RINGING
    val isMissed = call.status == CallStatus.MISSED
    val isMultiParty = call.participants.size > 2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onCallClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar with call status indicator
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMissed) Color(0xFFFF3B30).copy(alpha = 0.2f)
                            else GlassAccent.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMultiParty) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isMissed) Color(0xFFFF3B30) else GlassAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Call type indicator (top-right badge)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassAccent.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                        contentDescription = if (call.type == CallType.VIDEO) "Video call" else "Voice call",
                        tint = if (call.type == CallType.VIDEO) GlassAccent else Color(0xFF34C759),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Call direction icon
                    Icon(
                        imageVector = if (isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                        contentDescription = if (isIncoming) "Incoming" else "Outgoing",
                        tint = if (isMissed) Color(0xFFFF3B30) else Color(0xFF34C759),
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = call.initiator,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = GlassText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isMultiParty) {
                        Text(
                            text = "(+${call.participants.size - 2})",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCallDate(call.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassText.copy(alpha = 0.6f)
                    )

                    if (call.duration != null && call.duration > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassText.copy(alpha = 0.4f)
                        )
                        Text(
                            text = formatDuration(call.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassText.copy(alpha = 0.6f)
                        )
                    } else if (isMissed) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassText.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Missed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF3B30)
                        )
                    }
                }
            }
        }

        // Call back button
        IconButton(
            onClick = onCallClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFF34C759).copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Phone,
                contentDescription = "Call back",
                tint = Color(0xFF34C759),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyCallHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = GlassText.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No call history",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = GlassText.copy(alpha = 0.6f)
            )
            Text(
                text = "Search for a contact to make a call",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassText.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatCallDate(date: Date): String {
    val now = Calendar.getInstance()
    val callDate = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - callDate.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Yesterday"
        }
        now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds)
        else -> "${seconds}s"
    }
}
