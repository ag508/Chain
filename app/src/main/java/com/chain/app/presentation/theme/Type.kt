package com.chain.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chain.app.R

/**
 * Chain App Typography System
 * Based on chain-complete.html design specification
 *
 * Font Strategy:
 * - Zen Dots: Branding elements (Chain logo, main titles)
 * - Inter: All body text (matching HTML spec exactly)
 *
 * Inter provides excellent readability and is the font specified in
 * chain-complete.html for a modern, professional aesthetic.
 */

// Inter font family (400, 500, 600, 700, 800 weights from HTML spec)
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),     // 400
    Font(R.font.inter_medium, FontWeight.Medium),      // 500
    Font(R.font.inter_semibold, FontWeight.SemiBold),  // 600
    Font(R.font.inter_bold, FontWeight.Bold),          // 700
    Font(R.font.inter_extrabold, FontWeight.ExtraBold) // 800
)

// Zen Dots for branding elements (Chain logo, main title)
val ZenDotsFontFamily = FontFamily(
    Font(R.font.zendots_regular, FontWeight.Normal)
)

val Typography = Typography(
    // === DISPLAY (Brand elements, large hero text) ===
    // Use Zen Dots for largest brand elements
    displayLarge = TextStyle(
        fontFamily = ZenDotsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,  // Welcome title size from HTML
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,  // 800
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,  // 800
        fontSize = 28.sp,  // Auth title from HTML
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // === HEADLINE (Page titles, section headers) ===
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,  // 700
        fontSize = 28.sp,  // Auth title
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,  // 700
        fontSize = 24.sp,  // Biometric title
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,  // 600
        fontSize = 20.sp,  // Chat header
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // === TITLE (Card titles, feature titles) ===
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,  // 600
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,  // 600
        fontSize = 16.sp,  // Button text, feature title, chat name
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,  // 500
        fontSize = 14.sp,  // Tab text
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // === BODY (Primary content text) ===
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,  // 400
        fontSize = 16.sp,  // Input text, subtitle
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,  // 400
        fontSize = 15.sp,  // Auth subtitle, biometric subtitle
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,  // 400
        fontSize = 14.sp,  // Chat last message, feature desc
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // === LABEL (Buttons, tabs, small text) ===
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,  // 600
        fontSize = 14.sp,  // Tab label
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,  // 500
        fontSize = 13.sp,  // Chat time
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,  // 500
        fontSize = 12.sp,  // Nav label, small UI text
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)
