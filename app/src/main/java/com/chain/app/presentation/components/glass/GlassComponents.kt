package com.chain.app.presentation.components.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.glassCard
import com.chain.app.presentation.theme.glassButton
import com.chain.app.presentation.theme.glassTextField

/**
 * Glass-styled card component.
 *
 * @param modifier Modifier to be applied to the card
 * @param shape Shape of the card
 * @param onClick Optional click handler
 * @param content Content to be displayed inside the card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .glassCard(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Glass-styled button component.
 *
 * @param onClick Click handler
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param shape Shape of the button
 * @param contentPadding Padding around the button content
 * @param content Button content (text, icon, etc.)
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .glassButton(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Glass-styled text field component.
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier to be applied to the text field
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 * @param textStyle Style for the input text
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param supportingText Optional supporting text below the field
 * @param isError Whether the field is in error state
 * @param visualTransformation Visual transformation (e.g., password)
 * @param keyboardOptions Keyboard options
 * @param keyboardActions Keyboard actions
 * @param singleLine Whether the field is single-line
 * @param maxLines Maximum number of lines
 * @param shape Shape of the text field
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        // Text field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .glassTextField(shape)
                .padding(16.dp),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Leading icon
                    if (leadingIcon != null) {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            leadingIcon()
                        }
                    }

                    // Text field content
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Placeholder
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }

                    // Trailing icon
                    if (trailingIcon != null) {
                        Box(modifier = Modifier.padding(start = 8.dp)) {
                            trailingIcon()
                        }
                    }
                }
            }
        )

        // Supporting text / Error text
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Glass-styled icon button.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(48.dp)
            .glassButton(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Glass-styled floating action button.
 */
@Composable
fun GlassFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(56.dp)
            .glassButton(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
