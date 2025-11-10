package com.chain.app.presentation.settings.wallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperSelectorScreen(
    onBackClick: () -> Unit,
    viewModel: WallpaperSelectorViewModel = hiltViewModel()
) {
    val selectedWallpaper by viewModel.selectedWallpaper.collectAsState()
    val wallpapers = viewModel.availableWallpapers

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallpaper",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Selection Card
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current Wallpaper",
                        style = MaterialTheme.typography.titleSmall,
                        color = GlassTextSecondary
                    )

                    val currentWallpaper = wallpapers.find { it.id == selectedWallpaper }
                    if (currentWallpaper != null) {
                        WallpaperPreview(
                            wallpaper = currentWallpaper,
                            isSelected = true,
                            onSelect = {},
                            modifier = Modifier.height(120.dp)
                        )
                    }
                }
            }

            // Wallpaper Grid
            Text(
                text = "Available Wallpapers",
                style = MaterialTheme.typography.titleMedium,
                color = GlassAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(wallpapers) { wallpaper ->
                    WallpaperPreview(
                        wallpaper = wallpaper,
                        isSelected = wallpaper.id == selectedWallpaper,
                        onSelect = { viewModel.selectWallpaper(wallpaper.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperPreview(
    wallpaper: WallpaperOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = ChainSecureGreen,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = GlassTextSecondary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            )
            .background(
                brush = if (wallpaper.type == WallpaperType.GRADIENT) {
                    Brush.verticalGradient(
                        colors = listOf(
                            wallpaper.startColor,
                            wallpaper.endColor
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            wallpaper.startColor,
                            wallpaper.startColor
                        )
                    )
                }
            )
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        // Overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = when (wallpaper.type) {
                            WallpaperType.GRADIENT -> "Gradient"
                            WallpaperType.SOLID -> "Solid"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Wallpaper name and selection indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = ChainSecureGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = wallpaper.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
