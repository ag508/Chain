package com.chain.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Chain App Modern Color Palette
 * Premium black & white gradient theme with minimalist design
 * Based on app icon: black background with white chain links
 */

// === PRIMARY BLACK/WHITE/GRAY SCALE ===
val ChainBlack = Color(0xFF000000)
val ChainDarkGray = Color(0xFF1A1A1A)
val ChainMediumGray = Color(0xFF2D2D2D)
val ChainLightGray = Color(0xFF404040)
val ChainSilver = Color(0xFF808080)
val ChainLightSilver = Color(0xFFB8B8B8)
val ChainOffWhite = Color(0xFFF5F5F5)
val ChainWhite = Color(0xFFFFFFFF)

// === ACCENT COLORS (Minimal, for emphasis only) ===
val ChainAccent = Color(0xFF6366F1) // Indigo for CTAs and active states
val ChainSuccess = Color(0xFF10B981) // Success messages
val ChainError = Color(0xFFEF4444) // Errors and warnings
val ChainInfo = Color(0xFF3B82F6) // Info and links

// === GRADIENT COLORS ===
// Dark theme gradients (default)
val GradientDarkStart = Color(0xFF000000)
val GradientDarkMiddle = Color(0xFF1A1A1A)
val GradientDarkEnd = Color(0xFF2D2D2D)

// Light theme gradients
val GradientLightStart = Color(0xFFFFFFFF)
val GradientLightMiddle = Color(0xFFF5F5F5)
val GradientLightEnd = Color(0xFFE8E8E8)

// === GLASSMORPHISM OVERLAYS ===
val GlassWhite10 = Color(0x1AFFFFFF) // 10% white - subtle glass
val GlassWhite20 = Color(0x33FFFFFF) // 20% white - medium glass
val GlassWhite30 = Color(0x4DFFFFFF) // 30% white - strong glass
val GlassBlack10 = Color(0x1A000000) // 10% black - subtle shadow
val GlassBlack20 = Color(0x33000000) // 20% black - medium shadow

// === BRAND COLORS (From app icon) ===
val ChainLink = Color(0xFFFFFFFF) // White chain links
val ChainLinkShadow = Color(0xFF808080) // Chain shadow/depth

// === MESSAGE COLORS ===
val MessageSent = Color(0xFF2D2D2D) // Dark gray for sent messages
val MessageReceived = Color(0xFF1A1A1A) // Slightly darker for received
val MessageSentLight = Color(0xFFE8E8E8) // Light mode sent
val MessageReceivedLight = Color(0xFFF5F5F5) // Light mode received

// === STATUS COLORS ===
val StatusOnline = Color(0xFF10B981) // Online green
val StatusOffline = Color(0xFF6B7280) // Offline gray
val StatusTyping = Color(0xFF6366F1) // Typing indicator

// Legacy colors (keeping for backward compatibility, will phase out)
val ChainPrimary = ChainAccent
val ChainPrimaryDark = Color(0xFF4F46E5)
val ChainSecondary = ChainSilver
val ChainBackground = ChainWhite
val ChainBackgroundDark = ChainBlack
val ChainSurface = ChainOffWhite
val ChainSurfaceDark = ChainDarkGray
val ErrorColor = ChainError
val WarningColor = Color(0xFFF59E0B)
