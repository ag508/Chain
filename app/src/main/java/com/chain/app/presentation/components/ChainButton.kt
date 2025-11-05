package com.chain.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

@Composable
fun ChainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isAccent: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val surface = if (isDark) NeoDarkSurface else NeoLightSurface
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary
    val bgColor = if (isAccent) ChainSecureGreen else surface

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor.copy(alpha = if (enabled) 1f else 0.4f))
            .clickable(enabled = enabled && !isLoading, indication = null, interactionSource = interaction) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (isAccent) Color.White else textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChainSecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    ChainButton(text = text, onClick = onClick, isAccent = false, enabled = enabled)
}

@Composable
fun ChainTextButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Text(
        text = text,
        color = if (enabled) ChainSecureGreen else NeoDarkTextSecondary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    )
}
