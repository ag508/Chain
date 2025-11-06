package com.chain.app.presentation.chat.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Emoji picker for reactions and emoji input
 */
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .glassDialog(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose Emoji",
                style = MaterialTheme.typography.titleMedium,
                color = GlassText
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = GlassTextSecondary
                )
            }
        }

        // Emoji categories
        var selectedCategory by remember { mutableStateOf(EmojiCategory.FREQUENT) }

        TabRow(
            selectedTabIndex = EmojiCategory.entries.indexOf(selectedCategory),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = GlassAccent
        ) {
            EmojiCategory.entries.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            text = category.icon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedCategory.emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onEmojiSelected(emoji)
                            onDismiss()
                        }
                        .background(GlassAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

/**
 * Quick reaction picker (shown on long-press)
 */
@Composable
fun QuickReactionPicker(
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickReactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .glassDialog(shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickReactions.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        onReactionSelected(emoji)
                        onDismiss()
                    }
                    .background(GlassAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // More button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onDismiss)
                .background(GlassAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "➕",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Emoji categories
 */
enum class EmojiCategory(val icon: String, val emojis: List<String>) {
    FREQUENT(
        icon = "🕒",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "❤️", "👍", "👎", "✌️", "🤞", "🤟", "🤘", "👌",
            "🙏", "💪", "👏", "🙌", "🤝", "✨", "🎉", "🔥"
        )
    ),
    SMILEYS(
        icon = "😀",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "😚", "😙", "😋", "😛", "😜", "🤪",
            "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨",
            "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
            "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕"
        )
    ),
    GESTURES(
        icon = "👍",
        emojis = listOf(
            "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️",
            "🤟", "🤘", "👌", "🤏", "👈", "👉", "👆", "👇",
            "☝️", "👋", "🤚", "🖐", "✋", "🖖", "👏", "🙌",
            "👐", "🤲", "🤝", "🙏", "✍️", "💪", "🦾", "🦿"
        )
    ),
    HEARTS(
        icon = "❤️",
        emojis = listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖",
            "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉", "☸️"
        )
    ),
    ANIMALS(
        icon = "🐶",
        emojis = listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
            "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔",
            "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺"
        )
    ),
    FOOD(
        icon = "🍕",
        emojis = listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇",
            "🍓", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
            "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶", "🌽",
            "🥕", "🧄", "🧅", "🥔", "🍠", "🥐", "🥯", "🍞"
        )
    ),
    ACTIVITIES(
        icon = "⚽",
        emojis = listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
            "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
            "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿"
        )
    ),
    OBJECTS(
        icon = "💡",
        emojis = listOf(
            "⌚", "📱", "💻", "⌨️", "🖥", "🖨", "🖱", "🖲",
            "🕹", "🗜", "💾", "💿", "📀", "📼", "📷", "📸",
            "📹", "🎥", "📽", "🎞", "📞", "☎️", "📟", "📠"
        )
    )
}
