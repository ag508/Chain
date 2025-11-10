package com.chain.app.presentation.settings.network

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkUsageScreen(
    onBackClick: () -> Unit,
    viewModel: NetworkUsageViewModel = hiltViewModel()
) {
    val networkUsage by viewModel.networkUsage.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Network Usage",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Total Usage Card
            TotalUsageCard(
                totalBytes = networkUsage.totalBytes,
                bytesSent = networkUsage.totalBytesSent,
                bytesReceived = networkUsage.totalBytesReceived,
                resetDate = networkUsage.resetDate
            )

            // Usage Breakdown
            UsageBreakdownSection(networkUsage = networkUsage)

            // Reset Button
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GlassAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "About Network Usage",
                            style = MaterialTheme.typography.titleSmall,
                            color = GlassText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "• Network usage is tracked locally on your device\n" +
                                "• Statistics include both sent and received data\n" +
                                "• Usage is categorized by media type\n" +
                                "• Reset statistics to start tracking from zero",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.5
                    )
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Statistics?",
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassText
                )
            },
            text = {
                Text(
                    text = "This will reset all network usage statistics to zero. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetUsageStats()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = GlassText)
                }
            },
            containerColor = GlassGradientStart.copy(alpha = 0.95f)
        )
    }
}

@Composable
private fun TotalUsageCard(
    totalBytes: Long,
    bytesSent: Long,
    bytesReceived: Long,
    resetDate: Long?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Total Usage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassTextSecondary
                    )

                    Text(
                        text = totalBytes.formatBytes(),
                        style = MaterialTheme.typography.displaySmall,
                        color = GlassText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = GlassAccent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

            // Sent/Received breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UsageStatItem(
                    icon = Icons.Default.Upload,
                    label = "Sent",
                    value = bytesSent.formatBytes(),
                    color = ChainSecureGreen
                )

                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp),
                    color = GlassTextSecondary.copy(alpha = 0.2f)
                )

                UsageStatItem(
                    icon = Icons.Default.Download,
                    label = "Received",
                    value = bytesReceived.formatBytes(),
                    color = GlassAccent
                )
            }

            if (resetDate != null) {
                Text(
                    text = "Since ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(resetDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun UsageStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GlassTextSecondary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = GlassText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UsageBreakdownSection(networkUsage: NetworkUsage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Usage by Type",
            style = MaterialTheme.typography.titleMedium,
            color = GlassAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UsageTypeItem(
                    icon = Icons.Default.Message,
                    title = "Messages",
                    sent = networkUsage.messagesBytesSent.formatBytes(),
                    received = networkUsage.messagesBytesReceived.formatBytes(),
                    total = networkUsage.messagesTotal.formatBytes()
                )

                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                UsageTypeItem(
                    icon = Icons.Default.Image,
                    title = "Photos",
                    sent = networkUsage.photosBytesSent.formatBytes(),
                    received = networkUsage.photosBytesReceived.formatBytes(),
                    total = networkUsage.photosTotal.formatBytes()
                )

                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                UsageTypeItem(
                    icon = Icons.Default.VideoLibrary,
                    title = "Videos",
                    sent = networkUsage.videosBytesSent.formatBytes(),
                    received = networkUsage.videosBytesReceived.formatBytes(),
                    total = networkUsage.videosTotal.formatBytes()
                )

                Divider(color = GlassTextSecondary.copy(alpha = 0.2f))

                UsageTypeItem(
                    icon = Icons.Default.Description,
                    title = "Documents",
                    sent = networkUsage.documentsBytesSent.formatBytes(),
                    received = networkUsage.documentsBytesReceived.formatBytes(),
                    total = networkUsage.documentsTotal.formatBytes()
                )
            }
        }
    }
}

@Composable
private fun UsageTypeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sent: String,
    received: String,
    total: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(32.dp)
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "↑ $sent",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary
                )

                Text(
                    text = "↓ $received",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassTextSecondary
                )
            }
        }

        Text(
            text = total,
            style = MaterialTheme.typography.titleMedium,
            color = GlassText,
            fontWeight = FontWeight.Bold
        )
    }
}
