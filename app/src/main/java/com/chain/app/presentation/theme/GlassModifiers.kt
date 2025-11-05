package com.chain.app.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphic modifier extensions
 * Based on chain-complete.html design specification
 *
 * Note: Compose doesn't support true backdrop-filter blur,
 * so we simulate glassmorphism with semi-transparent backgrounds,
 * borders, and shadows.
 */

/**
 * Standard glass effect
 * - Semi-transparent background (rgba(20, 20, 20, 0.6) in dark mode)
 * - Subtle border (rgba(255, 255, 255, 0.1))
 * - Shadow for depth
 */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 8.dp
): Modifier {
    val isDark = true  // Default to dark theme as per HTML spec
    val bg = if (isDark) GlassCardBg else GlassLightCardBg
    val border = if (isDark) GlassBorder else GlassLightBorder
    val shadowColor = if (isDark) GlassShadow else GlassLightShadow

    return this
        .shadow(elevation = shadowElevation, shape = shape, ambientColor = shadowColor, spotColor = shadowColor)
        .clip(shape)
        .background(bg, shape)
        .border(width = 1.dp, color = border, shape = shape)
}

/**
 * Strong glass effect (more opaque, more blur simulation)
 * Used for important UI elements like popups and modals
 */
fun Modifier.glassStrong(
    shape: Shape = RoundedCornerShape(24.dp),
    shadowElevation: Dp = 20.dp
): Modifier {
    val isDark = true
    val bg = if (isDark) GlassSurface else GlassLightSurface
    val border = if (isDark) GlassBorder else GlassLightBorder
    val shadowColor = if (isDark) GlassShadow else GlassLightShadow

    return this
        .shadow(elevation = shadowElevation, shape = shape, ambientColor = shadowColor, spotColor = shadowColor)
        .clip(shape)
        .background(bg, shape)
        .border(width = 1.dp, color = border, shape = shape)
}

/**
 * Icon button glass effect
 * 40x40px with 12dp border radius from HTML spec
 */
fun Modifier.glassIconButton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val isDark = true
    val bg = if (isDark) GlassCardBg else GlassLightCardBg
    val border = if (isDark) GlassBorder else GlassLightBorder

    return this
        .shadow(elevation = 4.dp, shape = shape)
        .clip(shape)
        .background(bg, shape)
        .border(width = 1.dp, color = border, shape = shape)
}

/**
 * Feature card glass effect
 * Used for welcome screen feature cards
 */
fun Modifier.glassFeatureCard(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 8.dp)
}

/**
 * Auth icon container glass effect
 * 80x80px with 20dp border radius
 */
fun Modifier.glassAuthIcon(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 8.dp)
}

/**
 * Biometric icon container glass effect
 * 140x140px with 28dp border radius
 */
fun Modifier.glassBiometricIcon(
    shape: Shape = RoundedCornerShape(28.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 12.dp)
}

/**
 * App logo container glass effect
 * 120x120px with 30dp border radius
 */
fun Modifier.glassAppLogo(
    shape: Shape = RoundedCornerShape(30.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 8.dp)
}

/**
 * Input field glass effect
 * 16dp border radius, subtle styling
 */
fun Modifier.glassInput(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    val isDark = true
    val bg = if (isDark) GlassCardBg else GlassLightCardBg
    val border = if (isDark) GlassBorder else GlassLightBorder

    return this
        .clip(shape)
        .background(bg, shape)
        .border(width = 1.dp, color = border, shape = shape)
}

/**
 * OTP box glass effect
 * 56x64px with 16dp border radius
 */
fun Modifier.glassOtpBox(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 4.dp)
}

/**
 * Avatar glass effect
 * 52x52px with 16dp border radius (for chat list)
 */
fun Modifier.glassAvatar(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    val isDark = true
    val bg = if (isDark) GlassCardBg else GlassLightCardBg
    val border = if (isDark) GlassBorder else GlassLightBorder

    return this
        .clip(shape)
        .background(bg, shape)
        .border(width = 1.dp, color = border, shape = shape)
}

/**
 * Chat item hover glass effect
 * Applied on chat items when hovered/pressed
 */
fun Modifier.glassChatItemHover(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    return this.glass(shape = shape, shadowElevation = 4.dp)
}
