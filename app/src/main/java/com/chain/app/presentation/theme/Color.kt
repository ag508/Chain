package com.chain.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Chain App Glassmorphic Color Palette
 * Based on chain-complete.html design specification
 * Modern glassmorphism with backdrop blur effects
 */

// ===== DARK THEME (DEFAULT) =====
// Background & Surfaces
val GlassBg = Color(0xFF000000)  // Pure black background
val GlassSurface = Color(0x1E1E1EB2)  // rgba(30, 30, 30, 0.7)
val GlassCardBg = Color(0x14141499)  // rgba(20, 20, 20, 0.6)
val GlassSurfaceElevated = Color(0xFF1A1A1A)
val GlassBorder = Color(0x1AFFFFFF)  // rgba(255, 255, 255, 0.1)

// Text Colors
val GlassText = Color(0xFFFFFFFF)
val GlassTextSecondary = Color(0xFFB3B3B3)
val GlassTextMuted = Color(0xFF737373)

// Accent
val GlassAccent = Color(0xFFFFFFFF)  // White accent for dark theme
val GlassAccentHover = Color(0xFFE6E6E6)

// Borders & Dividers
val GlassDivider = Color(0x14FFFFFF)  // rgba(255, 255, 255, 0.08)

// Shadows & Overlays
val GlassShadow = Color(0x80000000)  // rgba(0, 0, 0, 0.5)
val GlassOverlay = Color(0xB3000000)  // rgba(0, 0, 0, 0.7)

// Gradients
val GlassGradientStart = Color(0xFF1A1A1A)
val GlassGradientEnd = Color(0xFF000000)

// ===== LIGHT THEME =====
val GlassLightBg = Color(0xFFF5F5F5)
val GlassLightSurface = Color(0xB2F0F0F0)  // rgba(240, 240, 240, 0.7)
val GlassLightCardBg = Color(0x99FFFFFF)  // rgba(255, 255, 255, 0.6)
val GlassLightBorder = Color(0x1A000000)  // rgba(0, 0, 0, 0.1)
val GlassLightText = Color(0xFF000000)
val GlassLightTextSecondary = Color(0xFF666666)
val GlassLightTextMuted = Color(0xFF999999)
val GlassLightAccent = Color(0xFF2D2D2D)
val GlassLightAccentHover = Color(0xFF000000)
val GlassLightShadow = Color(0x1A000000)  // rgba(0, 0, 0, 0.1)
val GlassLightGradientStart = Color(0xFFFFFFFF)
val GlassLightGradientEnd = Color(0xFFE0E0E0)

// ===== STATUS COLORS =====
val ChainError = Color(0xFFFF3333)
val ChainSuccess = Color(0xFF00CC66)
val ChainWarning = Color(0xFFFFCC00)

// Call status colors
val CallIncoming = Color(0xFF00CC66)
val CallOutgoing = GlassTextSecondary
val CallMissed = Color(0xFFFF3333)

// ===== BRAND COLORS (if needed for specific elements) =====
val ChainSecureGreen = Color(0xFF00C781)

// ===== BACKWARD COMPATIBILITY =====
// Map old color names to glassmorphic equivalents for gradual migration
val NeoDarkSurface = GlassCardBg
val NeoDarkTextPrimary = GlassText
val NeoDarkTextSecondary = GlassTextSecondary
val NeoDarkLightShadow = GlassSurface
val NeoDarkDarkShadow = GlassShadow
val NeoLightSurface = GlassLightCardBg
val NeoLightTextPrimary = GlassLightText
val NeoLightTextSecondary = GlassLightTextSecondary
val NeoLightLightShadow = GlassLightSurface
val NeoLightDarkShadow = GlassLightShadow
