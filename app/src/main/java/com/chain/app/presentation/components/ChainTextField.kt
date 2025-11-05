package com.chain.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

@Composable
fun ChainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val isDark = isSystemInDarkTheme()
    val surface = if (isDark) NeoDarkSurface else NeoLightSurface
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary
    val hintColor = if (isDark) NeoDarkTextSecondary else NeoLightTextSecondary

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = NeoDarkTextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(surface.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                cursorBrush = SolidColor(ChainSecureGreen),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(placeholder, color = hintColor.copy(alpha = 0.6f))
                        }
                        inner()
                    }
                }
            )
        }

        if (errorMessage != null) {
            Text(errorMessage, color = ChainError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
