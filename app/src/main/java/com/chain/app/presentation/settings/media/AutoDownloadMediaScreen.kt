package com.chain.app.presentation.settings.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*

@Composable
fun AutoDownloadMediaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoDownloadMediaViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .glassIconButton()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GlassText
                    )
                }

                Text(
                    text = "Auto-Download Media",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GlassText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // When using mobile data section
                MediaDownloadSection(
                    title = "When using mobile data",
                    photosEnabled = settings.mobileDataPhotos,
                    videosEnabled = settings.mobileDataVideos,
                    documentsEnabled = settings.mobileDataDocuments,
                    onPhotosChange = { viewModel.setMobileDataPhotos(it) },
                    onVideosChange = { viewModel.setMobileDataVideos(it) },
                    onDocumentsChange = { viewModel.setMobileDataDocuments(it) }
                )

                // When connected to Wi-Fi section
                MediaDownloadSection(
                    title = "When connected on Wi-Fi",
                    photosEnabled = settings.wifiPhotos,
                    videosEnabled = settings.wifiVideos,
                    documentsEnabled = settings.wifiDocuments,
                    onPhotosChange = { viewModel.setWifiPhotos(it) },
                    onVideosChange = { viewModel.setWifiVideos(it) },
                    onDocumentsChange = { viewModel.setWifiDocuments(it) }
                )

                // When roaming section
                MediaDownloadSection(
                    title = "When roaming",
                    photosEnabled = settings.roamingPhotos,
                    videosEnabled = settings.roamingVideos,
                    documentsEnabled = settings.roamingDocuments,
                    onPhotosChange = { viewModel.setRoamingPhotos(it) },
                    onVideosChange = { viewModel.setRoamingVideos(it) },
                    onDocumentsChange = { viewModel.setRoamingDocuments(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MediaDownloadSection(
    title: String,
    photosEnabled: Boolean,
    videosEnabled: Boolean,
    documentsEnabled: Boolean,
    onPhotosChange: (Boolean) -> Unit,
    onVideosChange: (Boolean) -> Unit,
    onDocumentsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = GlassAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass()
        ) {
            Column {
                MediaDownloadItem(
                    icon = Icons.Default.Image,
                    title = "Photos",
                    checked = photosEnabled,
                    onCheckedChange = onPhotosChange
                )
                Divider(
                    color = GlassBorder.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 56.dp)
                )
                MediaDownloadItem(
                    icon = Icons.Default.VideoLibrary,
                    title = "Videos",
                    checked = videosEnabled,
                    onCheckedChange = onVideosChange
                )
                Divider(
                    color = GlassBorder.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 56.dp)
                )
                MediaDownloadItem(
                    icon = Icons.Default.InsertDriveFile,
                    title = "Documents",
                    checked = documentsEnabled,
                    onCheckedChange = onDocumentsChange
                )
            }
        }
    }
}

@Composable
private fun MediaDownloadItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassAccent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GlassAccent,
                checkedTrackColor = GlassAccent.copy(alpha = 0.5f),
                uncheckedThumbColor = GlassText.copy(alpha = 0.5f),
                uncheckedTrackColor = GlassText.copy(alpha = 0.2f)
            )
        )
    }
}
