package com.chain.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Chain TextField - Glassmorphic Design
 * Based on chain-complete.html input specification
 *
 * .input:
 * - Background: glass with blur
 * - Padding: 16px vertical, 20px horizontal
 * - Border radius: 16dp
 * - Border: 1px solid glass-border
 * - Focus: border-color accent, box-shadow 0 0 0 3px rgba(255,255,255,0.1)
 */
@Composable
fun ChainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textAlign: TextAlign = TextAlign.Start
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animated border color on focus
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) GlassAccent else if (errorMessage != null) ChainError else GlassBorder,
        animationSpec = spring(),
        label = "border_color"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = GlassTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Input field with glass effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isFocused) 4.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = GlassShadow,
                    spotColor = GlassShadow
                )
                .clip(RoundedCornerShape(16.dp))
                .background(GlassCardBg)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                cursorBrush = SolidColor(GlassAccent),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = GlassText,
                    textAlign = textAlign
                ),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = GlassTextMuted
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = ChainError,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
