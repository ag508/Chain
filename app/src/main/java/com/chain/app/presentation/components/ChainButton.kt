package com.chain.app.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Chain Button - Glassmorphic Design
 * Based on chain-complete.html button specification
 *
 * Primary (.btn-primary):
 * - Background: accent color (white in dark, dark in light)
 * - Padding: 16px vertical, 32px horizontal
 * - Border radius: 16dp
 * - Hover: translateY(-2px), shadow
 *
 * Secondary (.btn-secondary):
 * - Background: glass with blur effect
 * - Border: 1px solid glass-border
 * - Same padding and radius as primary
 */
@Composable
fun ChainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isAccent: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Glassmorphic colors
    val bgColor = if (isAccent) GlassAccent else Color.Transparent
    val textColor = if (isAccent) GlassBg else GlassText  // White text on dark bg, vice versa
    val borderColor = if (!isAccent) GlassBorder else Color.Transparent

    // Hover/press elevation animation
    val elevation by animateDpAsState(
        targetValue = if (pressed) 0.dp else if (!isAccent) 4.dp else 8.dp,
        animationSpec = spring(),
        label = "btn_elevation"
    )

    // Press translation animation
    val translationY by animateDpAsState(
        targetValue = if (pressed) 0.dp else (-2).dp,
        animationSpec = spring(),
        label = "btn_translation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.translationY = if (pressed) 0f else translationY.toPx()
            }
            .then(
                if (isAccent) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor.copy(alpha = if (enabled) 1f else 0.4f))
                } else {
                    Modifier.glass(
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = elevation
                    )
                }
            )
            .clickable(
                enabled = enabled && !isLoading,
                indication = null,
                interactionSource = interaction
            ) {
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        }
    }
}

/**
 * Secondary button with glass effect
 */
@Composable
fun ChainSecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    ChainButton(
        text = text,
        onClick = onClick,
        isAccent = false,
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Text button for inline links
 */
@Composable
fun ChainTextButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Text(
        text = text,
        color = if (enabled) GlassText else GlassTextMuted,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    )
}
