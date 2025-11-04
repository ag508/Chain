package com.chain.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme - Black background with white accents (matches app icon)
 * Designed for OLED displays with pure black for battery efficiency
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - White for emphasis on dark background
    primary = ChainWhite,
    onPrimary = ChainBlack,
    primaryContainer = ChainMediumGray,
    onPrimaryContainer = ChainWhite,

    // Secondary colors - Accent for CTAs
    secondary = ChainAccent,
    onSecondary = ChainWhite,
    secondaryContainer = ChainDarkGray,
    onSecondaryContainer = ChainAccent,

    // Tertiary colors
    tertiary = ChainLightGray,
    onTertiary = ChainBlack,
    tertiaryContainer = ChainMediumGray,
    onTertiaryContainer = ChainLightGray,

    // Background and Surface - Gradient dark theme
    background = GradientDarkStart,
    onBackground = ChainWhite,
    surface = ChainDarkGray,
    onSurface = ChainWhite,
    surfaceVariant = ChainMediumGray,
    onSurfaceVariant = ChainLightGray,

    // Outline and borders
    outline = ChainMediumGray,
    outlineVariant = ChainDarkGray,

    // Status colors
    error = ChainError,
    onError = ChainWhite,
    errorContainer = ChainDarkGray,
    onErrorContainer = ChainError
)

/**
 * Light color scheme - White background with black accents
 * High contrast for readability and accessibility
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - Black for emphasis on light background
    primary = ChainBlack,
    onPrimary = ChainWhite,
    primaryContainer = ChainLightGray,
    onPrimaryContainer = ChainBlack,

    // Secondary colors - Accent for CTAs
    secondary = ChainAccent,
    onSecondary = ChainWhite,
    secondaryContainer = ChainLightestGray,
    onSecondaryContainer = ChainAccent,

    // Tertiary colors
    tertiary = ChainMediumGray,
    onTertiary = ChainWhite,
    tertiaryContainer = ChainLightGray,
    onTertiaryContainer = ChainBlack,

    // Background and Surface - Gradient light theme
    background = GradientLightStart,
    onBackground = ChainBlack,
    surface = ChainWhite,
    onSurface = ChainBlack,
    surfaceVariant = ChainLightestGray,
    onSurfaceVariant = ChainMediumGray,

    // Outline and borders
    outline = ChainLightGray,
    outlineVariant = ChainLightestGray,

    // Status colors
    error = ChainError,
    onError = ChainWhite,
    errorContainer = ChainLightestGray,
    onErrorContainer = ChainError
)

@Composable
fun ChainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use background color for immersive edge-to-edge experience
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            // Configure status bar icons color based on theme
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
