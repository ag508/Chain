package com.chain.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Neomorphic Chain text field with pressed (concave) effect.
 * Creates the appearance of text input pressed into the surface.
 */
@Composable
fun ChainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) NeoDarkSurface else NeoLightSurface
    val lightShadow = if (isDark) NeoDarkLightShadow else NeoLightLightShadow
    val darkShadow = if (isDark) NeoDarkDarkShadow else NeoLightDarkShadow
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary
    val secondaryTextColor = if (isDark) NeoDarkTextSecondary else NeoLightTextSecondary

    Column(modifier = modifier) {
        // Label above the field
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) ChainError else secondaryTextColor,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }

        // Neomorphic pressed input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(surfaceColor)
                .neomorphicPressed(
                    lightShadow = lightShadow,
                    darkShadow = darkShadow,
                    cornerRadius = 14.dp,
                    shadowBlur = 8.dp,
                    shadowOffset = 4.dp
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Leading icon
                if (leadingIcon != null) {
                    Box(modifier = Modifier.size(24.dp)) {
                        leadingIcon()
                    }
                }

                // Text input
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        enabled = enabled,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(if (isError) ChainError else ChainSecureGreen),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = secondaryTextColor.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Trailing icon
                if (trailingIcon != null) {
                    Box(modifier = Modifier.size(24.dp)) {
                        trailingIcon()
                    }
                }
            }
        }

        // Error message with smooth animation
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                color = ChainError,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }
    }
}
