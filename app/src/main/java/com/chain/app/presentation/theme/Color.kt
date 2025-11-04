package com.chain.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Chain App Neomorphic Color Palette
 * Soft, tactile design with extruded and pressed shadow effects
 * Based on WhatsApp layout with decentralized branding
 */

// ===== DARK MODE (PRIMARY) =====
// Base surface color for neomorphic design
val NeoDarkSurface = Color(0xFF2D343C)
val NeoDarkLightShadow = Color(0xFF3C444F)  // Top-left highlight
val NeoDarkDarkShadow = Color(0xFF1E242B)   // Bottom-right shadow

// Text colors
val NeoDarkTextPrimary = Color(0xFFFFFFFF)
val NeoDarkTextSecondary = Color(0xFF9A9A9A)

// ===== LIGHT MODE (SECONDARY) =====
// Base surface color for neomorphic design
val NeoLightSurface = Color(0xFFE0E5EC)
val NeoLightLightShadow = Color(0xFFFFFFFF)  // Top-left highlight
val NeoLightDarkShadow = Color(0xFFA3B1C6)   // Bottom-right shadow

// Text colors
val NeoLightTextPrimary = Color(0xFF333333)
val NeoLightTextSecondary = Color(0xFF6A6A6A)

// ===== BRAND & ACCENT COLORS =====
// "Secure Green" - primary brand color
val ChainSecureGreen = Color(0xFF00C781)
val ChainSecureGreenLight = Color(0xFF00E290)  // For green bubble highlights
val ChainSecureGreenDark = Color(0xFF00A36A)   // For green bubble shadows

// Status & alerts
val ChainError = Color(0xFFFF3B30)
val ChainWarning = Color(0xFFFFCC00)
val ChainSuccess = Color(0xFF34C759)

// Online/Offline status
val StatusOnline = ChainSecureGreen
val StatusOffline = Color(0xFF8E8E93)

// ===== MESSAGE BUBBLES =====
// Received messages use the base surface color with neomorphic shadows
// Sent messages use the secure green with adjusted shadows

// ===== LEGACY SUPPORT =====
// Keeping these for backward compatibility during transition
val ChainBlack = NeoDarkSurface
val ChainWhite = NeoLightSurface
val ChainAccent = ChainSecureGreen
val ChainPrimary = ChainSecureGreen
