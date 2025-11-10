package com.chain.app.presentation.settings.info

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
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FAQ",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = GlassAccent,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Frequently Asked Questions",
                            style = MaterialTheme.typography.titleLarge,
                            color = GlassText,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Find answers to common questions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            // FAQ Items
            val faqs = listOf(
                FAQItem(
                    question = "How does end-to-end encryption work?",
                    answer = "Chain uses end-to-end encryption (E2EE) to secure your messages. This means only you and the person you're communicating with can read the messages. Not even Chain can access your message content."
                ),
                FAQItem(
                    question = "Can I use Chain on multiple devices?",
                    answer = "Yes, you can use Chain on multiple devices. Your messages will sync across all devices where you're logged in with the same account."
                ),
                FAQItem(
                    question = "How do I backup my chats?",
                    answer = "Go to Settings > Chat Backup to configure automatic backups or manually backup your chats. You can choose backup frequency and what to include."
                ),
                FAQItem(
                    question = "Are my messages stored on servers?",
                    answer = "Messages are temporarily stored on our servers in encrypted form until they're delivered. Once delivered, they're removed from our servers. Local backups are stored on your device."
                ),
                FAQItem(
                    question = "How do I block someone?",
                    answer = "Open the chat with the person you want to block, tap their profile picture, and select 'Block User'. You can manage blocked contacts in Settings > Blocked Contacts."
                ),
                FAQItem(
                    question = "What happens when I delete a message?",
                    answer = "When you delete a message for yourself, it's removed from your device. If you delete for everyone, it's removed from all recipients' devices (if delivered within 48 hours)."
                ),
                FAQItem(
                    question = "How do I reduce data usage?",
                    answer = "Go to Settings > Auto-Download Media to control when media is automatically downloaded. You can disable auto-download on mobile data to save data."
                ),
                FAQItem(
                    question = "Can I use Chain without internet?",
                    answer = "You need an internet connection to send and receive messages. However, you can view previously loaded messages offline."
                ),
                FAQItem(
                    question = "How do I change my wallpaper?",
                    answer = "Go to Settings > Wallpaper to choose from various gradient and solid color options for your chat background."
                ),
                FAQItem(
                    question = "Are voice and video calls encrypted?",
                    answer = "Yes, all voice and video calls are end-to-end encrypted. No one, including Chain, can listen to or view your calls."
                )
            )

            faqs.forEach { faq ->
                ExpandableFAQCard(faq = faq)
            }
        }
    }
}

@Composable
private fun ExpandableFAQCard(faq: FAQItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = GlassAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                )
            }
        }
    }
}

private data class FAQItem(
    val question: String,
    val answer: String
)
