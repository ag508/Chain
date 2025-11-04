package com.chain.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neomorphic shadow effects for Chain app.
 * Creates soft, tactile UI with extruded (raised) and pressed (concave) effects.
 */

/**
 * Extruded (Raised) neomorphic effect.
 * Used for buttons, list items, and clickable elements.
 * Creates appearance of element rising from the surface.
 *
 * @param lightShadow Top-left highlight color
 * @param darkShadow Bottom-right shadow color
 * @param cornerRadius Corner radius of the shape
 * @param shadowBlur Blur amount for shadows
 * @param shadowOffset Offset distance for shadows
 */
fun Modifier.neomorphicExtruded(
    lightShadow: Color,
    darkShadow: Color,
    cornerRadius: Dp = 16.dp,
    shadowBlur: Dp = 10.dp,
    shadowOffset: Dp = 6.dp
): Modifier = this.drawBehind {
    val cornerRadiusPx = cornerRadius.toPx()
    val shadowBlurPx = shadowBlur.toPx()
    val shadowOffsetPx = shadowOffset.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        // Draw light shadow (top-left)
        frameworkPaint.color = lightShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            -shadowOffsetPx,
            -shadowOffsetPx,
            lightShadow.copy(alpha = 0.6f).toArgb()
        )
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadiusPx,
            radiusY = cornerRadiusPx,
            paint = paint
        )

        // Draw dark shadow (bottom-right)
        frameworkPaint.color = darkShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            shadowOffsetPx,
            shadowOffsetPx,
            darkShadow.copy(alpha = 0.6f).toArgb()
        )
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadiusPx,
            radiusY = cornerRadiusPx,
            paint = paint
        )
    }
}

/**
 * Pressed (Concave) neomorphic effect.
 * Used for active/toggled states and input fields.
 * Creates appearance of element pressed into the surface.
 *
 * @param lightShadow Top-left inner shadow color
 * @param darkShadow Bottom-right inner shadow color
 * @param cornerRadius Corner radius of the shape
 * @param shadowBlur Blur amount for inner shadows
 * @param shadowOffset Offset distance for inner shadows
 */
fun Modifier.neomorphicPressed(
    lightShadow: Color,
    darkShadow: Color,
    cornerRadius: Dp = 16.dp,
    shadowBlur: Dp = 8.dp,
    shadowOffset: Dp = 4.dp
): Modifier = this.drawBehind {
    val cornerRadiusPx = cornerRadius.toPx()
    val shadowBlurPx = shadowBlur.toPx()
    val shadowOffsetPx = shadowOffset.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        // Draw dark inner shadow (top-left)
        frameworkPaint.color = darkShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            shadowOffsetPx,
            shadowOffsetPx,
            darkShadow.copy(alpha = 0.8f).toArgb()
        )
        canvas.drawRoundRect(
            left = shadowOffsetPx,
            top = shadowOffsetPx,
            right = size.width - shadowOffsetPx,
            bottom = size.height - shadowOffsetPx,
            radiusX = cornerRadiusPx,
            radiusY = cornerRadiusPx,
            paint = paint
        )

        // Draw light inner shadow (bottom-right)
        frameworkPaint.color = lightShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            -shadowOffsetPx,
            -shadowOffsetPx,
            lightShadow.copy(alpha = 0.4f).toArgb()
        )
        canvas.drawRoundRect(
            left = shadowOffsetPx,
            top = shadowOffsetPx,
            right = size.width - shadowOffsetPx,
            bottom = size.height - shadowOffsetPx,
            radiusX = cornerRadiusPx,
            radiusY = cornerRadiusPx,
            paint = paint
        )
    }
}

/**
 * Circular extruded effect for FABs and round buttons.
 */
fun Modifier.neomorphicCircleExtruded(
    lightShadow: Color,
    darkShadow: Color,
    shadowBlur: Dp = 10.dp,
    shadowOffset: Dp = 6.dp
): Modifier = this.drawBehind {
    val shadowBlurPx = shadowBlur.toPx()
    val shadowOffsetPx = shadowOffset.toPx()
    val radius = size.minDimension / 2

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val center = Offset(size.width / 2, size.height / 2)

        // Draw light shadow (top-left)
        frameworkPaint.color = lightShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            -shadowOffsetPx,
            -shadowOffsetPx,
            lightShadow.copy(alpha = 0.6f).toArgb()
        )
        canvas.drawCircle(center, radius, paint)

        // Draw dark shadow (bottom-right)
        frameworkPaint.color = darkShadow.toArgb()
        frameworkPaint.setShadowLayer(
            shadowBlurPx,
            shadowOffsetPx,
            shadowOffsetPx,
            darkShadow.copy(alpha = 0.6f).toArgb()
        )
        canvas.drawCircle(center, radius, paint)
    }
}
