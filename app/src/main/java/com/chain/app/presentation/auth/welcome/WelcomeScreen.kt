package com.chain.app.presentation.auth.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.app.R
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.theme.*

/**
 * Welcome Screen - Glassmorphic Design
 * Based on chain-complete.html specification
 *
 * Features:
 * - Gradient background (135deg linear)
 * - 120dp app logo with glass effect
 * - Zen Dots title font (42sp, 800 weight)
 * - Glass feature cards (20dp padding, vertical layout)
 * - Primary and secondary glass buttons
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_animation")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    // Pulse animation for logo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    val gradientBackground = Brush.linearGradient(
        colors = listOf(
            GlassGradientStart,
            GlassGradientEnd
        ),
        start = Offset(0f, offset),
        end = Offset(offset, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Logo with pulse animation - fills entire rounded box
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(RoundedCornerShape(30.dp))
                    .glassAppLogo(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Chain Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title - Zen Dots font, 42sp, 800 weight
            Text(
                text = "Chain",
                style = MaterialTheme.typography.displayLarge,
                color = GlassText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle - 16sp, Inter regular
            Text(
                text = "Secure, private, and decentralized messaging for everyone",
                style = MaterialTheme.typography.bodyLarge,
                color = GlassTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Feature cards - vertical layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature 1: End-to-End Encrypted
                FeatureCard(
                    icon = Icons.Outlined.Lock,
                    title = "End-to-End Encrypted",
                    description = "Your messages are secure and private"
                )

                // Feature 2: Decentralized Network
                FeatureCard(
                    icon = Icons.Outlined.Link,
                    title = "Decentralized Network",
                    description = "No central server, complete control"
                )

                // Feature 3: Lightning Fast
                FeatureCard(
                    icon = Icons.Outlined.Bolt,
                    title = "Lightning Fast",
                    description = "Instant message delivery worldwide"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary button
                ChainButton(
                    text = "Get Started",
                    onClick = onGetStartedClick,
                    isAccent = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary button
                ChainButton(
                    text = "Learn More",
                    onClick = { /* TODO: Navigate to learn more */ },
                    isAccent = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Feature card with glass effect
 * 20dp padding, 20dp border radius
 * Horizontal layout with icon and content
 */
@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassFeatureCard()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container - 48x48dp with 14dp border radius
        Box(
            modifier = Modifier
                .size(48.dp)
                .glass(shape = RoundedCornerShape(14.dp), shadowElevation = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = GlassText
            )
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = GlassText
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTextSecondary
            )
        }
    }
}
