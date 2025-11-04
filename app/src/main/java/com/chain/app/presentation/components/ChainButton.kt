package com.chain.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Neomorphic Chain button with extruded (raised) effect.
 * Animates to pressed state on interaction.
 */
@Composable
fun ChainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isAccent: Boolean = true // If true, uses secure green. If false, uses surface color
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Colors based on theme and accent
    val surfaceColor = if (isDark) NeoDarkSurface else NeoLightSurface
    val lightShadow = if (isDark) NeoDarkLightShadow else NeoLightLightShadow
    val darkShadow = if (isDark) NeoDarkDarkShadow else NeoLightDarkShadow
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary

    // For accent button (secure green)
    val buttonColor = if (isAccent) ChainSecureGreen else surfaceColor
    val buttonTextColor = if (isAccent) Color.White else textColor

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.96f
            isPressed -> 0.98f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(buttonColor)
            .then(
                if (isPressed || !enabled) {
                    // Pressed state - concave
                    Modifier.neomorphicPressed(
                        lightShadow = if (isAccent) ChainSecureGreenLight else lightShadow,
                        darkShadow = if (isAccent) ChainSecureGreenDark else darkShadow,
                        cornerRadius = 16.dp,
                        shadowBlur = 6.dp,
                        shadowOffset = 3.dp
                    )
                } else {
                    // Default state - extruded
                    Modifier.neomorphicExtruded(
                        lightShadow = if (isAccent) ChainSecureGreenLight else lightShadow,
                        darkShadow = if (isAccent) ChainSecureGreenDark else darkShadow,
                        cornerRadius = 16.dp,
                        shadowBlur = 10.dp,
                        shadowOffset = 6.dp
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = buttonTextColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = buttonTextColor.copy(alpha = if (enabled) 1f else 0.5f)
            )
        }
    }
}

/**
 * Neomorphic secondary button (surface color, not accent).
 */
@Composable
fun ChainSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ChainButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = false,
        isAccent = false // Uses surface color, not green
    )
}

/**
 * Text button variant with accent color (for "Skip", "Sign In", etc.).
 */
@Composable
fun ChainTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = ChainSecureGreen,
            disabledContentColor = if (isSystemInDarkTheme()) NeoDarkTextSecondary else NeoLightTextSecondary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
