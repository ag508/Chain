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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chain.app.domain.model.Message
import com.chain.app.presentation.theme.*

/**
 * Message search bar with results
 */
@Composable
fun MessageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<Message>,
    currentResultIndex: Int,
    onNavigateToResult: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search field
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search in conversation...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GlassTextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = GlassTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassAccent,
                    unfocusedBorderColor = GlassTextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = GlassText,
                    unfocusedTextColor = GlassText
                )
            )

            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassAccent.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = GlassText
                )
            }
        }

        // Navigation controls (when there are results)
        if (searchResults.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentResultIndex + 1} of ${searchResults.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Previous result
                    IconButton(
                        onClick = {
                            if (currentResultIndex > 0) {
                                onNavigateToResult(currentResultIndex - 1)
                            }
                        },
                        enabled = currentResultIndex > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassAccent.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Previous result",
                            tint = if (currentResultIndex > 0) GlassText else GlassTextSecondary
                        )
                    }

                    // Next result
                    IconButton(
                        onClick = {
                            if (currentResultIndex < searchResults.size - 1) {
                                onNavigateToResult(currentResultIndex + 1)
                            }
                        },
                        enabled = currentResultIndex < searchResults.size - 1,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassAccent.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Next result",
                            tint = if (currentResultIndex < searchResults.size - 1) GlassText else GlassTextSecondary
                        )
                    }
                }
            }
        }

        // No results message
        if (query.isNotEmpty() && searchResults.isEmpty()) {
            Text(
                text = "No messages found",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * Search result item showing message preview
 */
@Composable
fun SearchResultItem(
    message: Message,
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(GlassAccent.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Message icon
        Icon(
            imageVector = Icons.Default.Message,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(20.dp)
        )

        // Message content with highlighted query
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Sender name
            Text(
                text = message.senderId, // TODO: Get actual sender name
                style = MaterialTheme.typography.labelSmall,
                color = GlassAccent,
                fontWeight = FontWeight.SemiBold
            )

            // Message content with highlighted search query
            Text(
                text = highlightQuery(message.content, query),
                style = MaterialTheme.typography.bodyMedium,
                color = GlassText,
                maxLines = 2
            )

            // Timestamp
            Text(
                text = formatSearchResultTime(message.timestamp.time),
                style = MaterialTheme.typography.labelSmall,
                color = GlassTextSecondary
            )
        }

        // Navigate icon
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go to message",
            tint = GlassTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Highlight search query in text
 */
@Composable
private fun highlightQuery(text: String, query: String) = buildAnnotatedString {
    if (query.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var currentIndex = 0

    while (currentIndex < text.length) {
        val queryIndex = lowerText.indexOf(lowerQuery, currentIndex)
        if (queryIndex == -1) {
            append(text.substring(currentIndex))
            break
        }

        // Text before match
        append(text.substring(currentIndex, queryIndex))

        // Highlighted match
        withStyle(
            style = SpanStyle(
                color = GlassAccent,
                fontWeight = FontWeight.Bold,
                background = GlassAccent.copy(alpha = 0.2f)
            )
        ) {
            append(text.substring(queryIndex, queryIndex + query.length))
        }

        currentIndex = queryIndex + query.length
    }
}

private fun formatSearchResultTime(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    val messageCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    return when {
        calendar.get(java.util.Calendar.DAY_OF_YEAR) == messageCalendar.get(java.util.Calendar.DAY_OF_YEAR) &&
                calendar.get(java.util.Calendar.YEAR) == messageCalendar.get(java.util.Calendar.YEAR) -> {
            // Today - show time only
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        }
        calendar.get(java.util.Calendar.YEAR) == messageCalendar.get(java.util.Calendar.YEAR) -> {
            // This year - show month and day
            java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        }
        else -> {
            // Other years - show full date
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        }
    }
}
