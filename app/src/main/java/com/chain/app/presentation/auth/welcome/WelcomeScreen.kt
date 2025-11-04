package com.chain.app.presentation.auth.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*

/**
 * Neomorphic Welcome/Splash screen for Chain app.
 * Clean, minimalist design with soft shadows and "Chain" branding.
 * Tagline: "Decentralized. Secure. Yours."
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) NeoDarkSurface else NeoLightSurface
    val lightShadow = if (isDark) NeoDarkLightShadow else NeoLightLightShadow
    val darkShadow = if (isDark) NeoDarkDarkShadow else NeoLightDarkShadow
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary
    val secondaryTextColor = if (isDark) NeoDarkTextSecondary else NeoLightTextSecondary

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "content_alpha"
    )

    // Main container with neomorphic surface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState())
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo and branding section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo - neomorphic extruded circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .clip(CircleShape)
                        .background(surfaceColor)
                        .neomorphicExtruded(
                            lightShadow = lightShadow,
                            darkShadow = darkShadow,
                            cornerRadius = 60.dp,
                            shadowBlur = 15.dp,
                            shadowOffset = 8.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⛓️",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.2f
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // App Name - "Chain" in Zen Dots font (TODO: add custom font)
                Text(
                    text = "Chain",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tagline
                Text(
                    text = "Decentralized. Secure. Yours.",
                    style = MaterialTheme.typography.titleMedium,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Feature highlights - simple text, no cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureItem(
                        icon = "🔒",
                        title = "End-to-End Encrypted",
                        description = "Your messages stay private with Signal Protocol",
                        textColor = textColor,
                        secondaryColor = secondaryTextColor
                    )

                    FeatureItem(
                        icon = "🌐",
                        title = "Decentralized Network",
                        description = "No central servers, true peer-to-peer messaging",
                        textColor = textColor,
                        secondaryColor = secondaryTextColor
                    )

                    FeatureItem(
                        icon = "⛓️",
                        title = "Blockchain Verified",
                        description = "Messages authenticated on-chain for trust",
                        textColor = textColor,
                        secondaryColor = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChainButton(
                    text = "Get Started",
                    onClick = onGetStartedClick,
                    isAccent = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ChainTextButton(
                        text = "Sign In",
                        onClick = onSignInClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryTextColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    description: String,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor
            )
        }
    }
}
