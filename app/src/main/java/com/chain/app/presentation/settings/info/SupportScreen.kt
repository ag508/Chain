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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Support",
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GlassGradientStart, GlassGradientEnd)
                )
            )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(16.dp)),
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
                        imageVector = Icons.Default.Support,
                        contentDescription = null,
                        tint = GlassAccent,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Get Help",
                            style = MaterialTheme.typography.titleLarge,
                            color = GlassText,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "We're here to help you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            // Contact Methods
            Text(
                text = "Contact Us",
                style = MaterialTheme.typography.titleMedium,
                color = GlassAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            ContactMethodCard(
                icon = Icons.Default.Email,
                title = "Email Support",
                description = "support@chain-app.com",
                subtitle = "Response within 24 hours"
            )

            ContactMethodCard(
                icon = Icons.Default.Share,
                title = "Twitter",
                description = "@ChainApp",
                subtitle = "Follow us for updates"
            )

            ContactMethodCard(
                icon = Icons.Default.Language,
                title = "Website",
                description = "www.chain-app.com",
                subtitle = "Visit our website"
            )

            ContactMethodCard(
                icon = Icons.Default.Forum,
                title = "Community Forum",
                description = "community.chain-app.com",
                subtitle = "Connect with other users"
            )

            // Additional Resources
            Text(
                text = "Additional Resources",
                style = MaterialTheme.typography.titleMedium,
                color = GlassAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResourceItem(
                        icon = Icons.Default.MenuBook,
                        title = "User Guide",
                        description = "Learn how to use Chain"
                    )

                    Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                    ResourceItem(
                        icon = Icons.Default.VideoLibrary,
                        title = "Video Tutorials",
                        description = "Watch step-by-step guides"
                    )

                    Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                    ResourceItem(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        description = "Help us improve Chain"
                    )

                    Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                    ResourceItem(
                        icon = Icons.Default.Lightbulb,
                        title = "Feature Request",
                        description = "Suggest new features"
                    )
                }
            }

            // App Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Chain Messenger",
                        style = MaterialTheme.typography.titleMedium,
                        color = GlassText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )

                    Text(
                        text = "© 2024 Chain App. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard()
            .clickable { /* Handle click */ },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlassAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassAccent
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GlassTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ResourceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Handle click */ }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = GlassText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            tint = GlassTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
