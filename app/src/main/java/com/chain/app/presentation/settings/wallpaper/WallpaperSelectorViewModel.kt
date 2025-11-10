package com.chain.app.presentation.settings.wallpaper

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.app.data.preferences.UserPreferences
import com.chain.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperSelectorViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val selectedWallpaper: StateFlow<String> = userPreferences.getSelectedWallpaper()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "gradient_default"
        )

    fun selectWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            userPreferences.setSelectedWallpaper(wallpaperId)
        }
    }

    // Predefined wallpapers
    val availableWallpapers = listOf(
        WallpaperOption(
            id = "gradient_default",
            name = "Default Gradient",
            type = WallpaperType.GRADIENT,
            startColor = GlassGradientStart,
            endColor = GlassGradientEnd
        ),
        WallpaperOption(
            id = "gradient_ocean",
            name = "Ocean Blue",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF1E3A8A),
            endColor = Color(0xFF3B82F6)
        ),
        WallpaperOption(
            id = "gradient_sunset",
            name = "Sunset",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFFEC4899),
            endColor = Color(0xFFF59E0B)
        ),
        WallpaperOption(
            id = "gradient_forest",
            name = "Forest Green",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF064E3B),
            endColor = Color(0xFF10B981)
        ),
        WallpaperOption(
            id = "gradient_purple",
            name = "Purple Haze",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF581C87),
            endColor = Color(0xFFA855F7)
        ),
        WallpaperOption(
            id = "gradient_crimson",
            name = "Crimson",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF7F1D1D),
            endColor = Color(0xFFEF4444)
        ),
        WallpaperOption(
            id = "solid_dark",
            name = "Dark",
            type = WallpaperType.SOLID,
            startColor = Color(0xFF111827),
            endColor = Color(0xFF111827)
        ),
        WallpaperOption(
            id = "solid_black",
            name = "Pure Black",
            type = WallpaperType.SOLID,
            startColor = Color.Black,
            endColor = Color.Black
        ),
        WallpaperOption(
            id = "gradient_midnight",
            name = "Midnight",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF0F172A),
            endColor = Color(0xFF1E293B)
        ),
        WallpaperOption(
            id = "gradient_aurora",
            name = "Aurora",
            type = WallpaperType.GRADIENT,
            startColor = Color(0xFF4C1D95),
            endColor = Color(0xFF06B6D4)
        )
    )
}

data class WallpaperOption(
    val id: String,
    val name: String,
    val type: WallpaperType,
    val startColor: Color,
    val endColor: Color
)

enum class WallpaperType {
    SOLID,
    GRADIENT
}
