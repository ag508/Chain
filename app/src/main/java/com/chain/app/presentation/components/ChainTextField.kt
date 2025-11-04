package com.chain.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

/**
 * Custom Chain text field with modern glassmorphic design.
 * Features smooth animations and high-contrast colors for accessibility.
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
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = if (label != null) {
                {
                    Text(
                        text = label,
                        color = if (isError) ChainError else ChainLightGray
                    )
                }
            } else null,
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ChainMediumGray
                    )
                }
            } else null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Text colors
                focusedTextColor = ChainWhite,
                unfocusedTextColor = ChainWhite,
                disabledTextColor = ChainMediumGray,
                errorTextColor = ChainWhite,

                // Border colors
                focusedBorderColor = ChainAccent,
                unfocusedBorderColor = GlassWhite20,
                disabledBorderColor = GlassWhite10,
                errorBorderColor = ChainError,

                // Container colors
                focusedContainerColor = GlassWhite5,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                errorContainerColor = GlassWhite5,

                // Cursor color
                cursorColor = ChainAccent,
                errorCursorColor = ChainError,

                // Label colors (focused/unfocused)
                focusedLabelColor = ChainAccent,
                unfocusedLabelColor = ChainLightGray,
                disabledLabelColor = ChainMediumGray,
                errorLabelColor = ChainError
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

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
                modifier = Modifier.padding(start = 16.dp, top = 6.dp)
            )
        }
    }
}
