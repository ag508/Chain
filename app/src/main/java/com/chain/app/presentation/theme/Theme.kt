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
 * Dark color scheme - Neomorphic soft UI (Primary)
 * Single surface color with shadow-based depth
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Secure Green accent
    primary = ChainSecureGreen,
    onPrimary = NeoDarkTextPrimary,
    primaryContainer = NeoDarkSurface,
    onPrimaryContainer = ChainSecureGreen,

    // Secondary - Surface variations
    secondary = NeoDarkLightShadow,
    onSecondary = NeoDarkTextPrimary,
    secondaryContainer = NeoDarkSurface,
    onSecondaryContainer = NeoDarkTextSecondary,

    // Tertiary - Shadow variations
    tertiary = NeoDarkDarkShadow,
    onTertiary = NeoDarkTextPrimary,
    tertiaryContainer = NeoDarkSurface,
    onTertiaryContainer = NeoDarkTextSecondary,

    // Background and Surface - All use the same neomorphic base
    background = NeoDarkSurface,
    onBackground = NeoDarkTextPrimary,
    surface = NeoDarkSurface,
    onSurface = NeoDarkTextPrimary,
    surfaceVariant = NeoDarkSurface,
    onSurfaceVariant = NeoDarkTextSecondary,

    // Outline - subtle for neomorphism
    outline = NeoDarkDarkShadow,
    outlineVariant = NeoDarkLightShadow,

    // Status colors
    error = ChainError,
    onError = NeoDarkTextPrimary,
    errorContainer = NeoDarkSurface,
    onErrorContainer = ChainError
)

/**
 * Light color scheme - Neomorphic soft UI (Secondary)
 * Single surface color with shadow-based depth
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Secure Green accent
    primary = ChainSecureGreen,
    onPrimary = NeoLightTextPrimary,
    primaryContainer = NeoLightSurface,
    onPrimaryContainer = ChainSecureGreen,

    // Secondary - Surface variations
    secondary = NeoLightLightShadow,
    onSecondary = NeoLightTextPrimary,
    secondaryContainer = NeoLightSurface,
    onSecondaryContainer = NeoLightTextSecondary,

    // Tertiary - Shadow variations
    tertiary = NeoLightDarkShadow,
    onTertiary = NeoLightTextPrimary,
    tertiaryContainer = NeoLightSurface,
    onTertiaryContainer = NeoLightTextSecondary,

    // Background and Surface - All use the same neomorphic base
    background = NeoLightSurface,
    onBackground = NeoLightTextPrimary,
    surface = NeoLightSurface,
    onSurface = NeoLightTextPrimary,
    surfaceVariant = NeoLightSurface,
    onSurfaceVariant = NeoLightTextSecondary,

    // Outline - subtle for neomorphism
    outline = NeoLightDarkShadow,
    outlineVariant = NeoLightLightShadow,

    // Status colors
    error = ChainError,
    onError = NeoLightTextPrimary,
    errorContainer = NeoLightSurface,
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
