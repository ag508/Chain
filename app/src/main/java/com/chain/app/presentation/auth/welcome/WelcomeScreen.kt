package com.chain.app.presentation.auth.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*

/**
 * Welcome/Onboarding screen with glassmorphism and modern minimalist design.
 * Features black/white gradient background matching app icon aesthetic.
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Floating animation for background elements
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "content_alpha"
    )

    val contentOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "content_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientDarkStart,
                        GradientDarkEnd
                    )
                )
            )
    ) {
        // Decorative floating circles for depth (glassmorphism effect)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp, y = (100 + floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(50.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = 200.dp, y = (400 - floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo and branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .offset(y = contentOffset)
            ) {
                // Logo with glassmorphic container
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(logoScale)
                        .background(
                            color = GlassWhite10,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⛓️",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.8f
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Chain",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChainWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Decentralized Messaging",
                    style = MaterialTheme.typography.titleLarge,
                    color = ChainLightGray,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Feature highlights in glassmorphic card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = GlassWhite10
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FeatureItem(
                            icon = "🔒",
                            title = "End-to-End Encrypted",
                            description = "Signal Protocol security"
                        )

                        Divider(
                            color = GlassWhite10,
                            thickness = 1.dp
                        )

                        FeatureItem(
                            icon = "🌐",
                            title = "Decentralized",
                            description = "True peer-to-peer messaging"
                        )

                        Divider(
                            color = GlassWhite10,
                            thickness = 1.dp
                        )

                        FeatureItem(
                            icon = "⛓️",
                            title = "Blockchain Verified",
                            description = "Authenticated on-chain"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
                    .offset(y = -contentOffset),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChainButton(
                    text = "Get Started",
                    onClick = onGetStartedClick
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChainLightGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ChainTextButton(
                        text = "Sign In",
                        onClick = onSignInClick
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChainMediumGray,
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
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = GlassWhite10,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ChainWhite
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ChainLightGray
            )
        }
    }
}
