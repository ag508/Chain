package com.chain.app.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow

/**
 * Glass morphism effects for Chain UI.
 * Provides reusable modifiers for creating glassmorphism effect.
 */

/**
 * Apply glassmorphism effect to a composable.
 *
 * @param shape The shape of the glass element
 * @param blurRadius The amount of blur (0dp = no blur)
 * @param alpha The opacity of the background (0f = transparent, 1f = opaque)
 * @param borderAlpha The opacity of the border
 * @param elevation The shadow elevation
 */
fun Modifier.glass(
    shape: Shape,
    blurRadius: Dp = 12.dp,
    alpha: Float = 0.15f,
    borderAlpha: Float = 0.3f,
    elevation: Dp = 4.dp
): Modifier = composed {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = alpha)
    } else {
        Color.Black.copy(alpha = (alpha + 0.1f).coerceAtMost(1.0f))
    }

    val borderColor = if (isLight) {
        Color.White.copy(alpha = borderAlpha)
    } else {
        Color.Gray.copy(alpha = borderAlpha)
    }

    this
        .shadow(elevation, shape)
        .border(1.dp, borderColor, shape)
        .background(backgroundColor, shape)
        .clip(shape)
}

/**
 * Lighter glass effect for cards and containers.
 */
fun Modifier.glassCard(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 16.dp,
        alpha = 0.12f,
        borderAlpha = 0.25f,
        elevation = 6.dp
    )
}

/**
 * Glass effect for buttons and interactive elements.
 */
fun Modifier.glassButton(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 10.dp,
        alpha = 0.18f,
        borderAlpha = 0.35f,
        elevation = 4.dp
    )
}

/**
 * Glass effect for input fields.
 */
fun Modifier.glassTextField(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 14.dp,
        alpha = 0.10f,
        borderAlpha = 0.20f,
        elevation = 2.dp
    )
}

/**
 * Glass effect for dialogs and modals.
 */
fun Modifier.glassDialog(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 0.dp,
        alpha = 1.0f,
        borderAlpha = 0.30f,
        elevation = 12.dp
    )
}

/**
 * Glass effect for app bars.
 */
fun Modifier.glassTopBar(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 16.dp,
        alpha = 0.15f,
        borderAlpha = 0.25f,
        elevation = 8.dp
    )
}

/**
 * Glass effect for bottom bars and navigation.
 */
fun Modifier.glassBottomBar(shape: Shape): Modifier = composed {
    glass(
        shape = shape,
        blurRadius = 16.dp,
        alpha = 0.15f,
        borderAlpha = 0.25f,
        elevation = 8.dp
    )
}

/**
 * Calculate luminance of a color (for determining light/dark theme).
 */
private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
