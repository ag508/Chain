package com.chain.app.presentation.auth.biometric

import androidx.compose.animation.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainSecondaryButton
import com.chain.app.presentation.theme.*

/**
 * Biometric setup screen with modern glassmorphic design.
 * Optional security enhancement with clean, minimalist interface.
 */
@Composable
fun BiometricSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: BiometricSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate when setup is complete
    LaunchedEffect(state.setupComplete) {
        if (state.setupComplete) {
            onSetupComplete()
        }
    }

    // Animations
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "content_alpha"
    )

    val contentOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "content_offset"
    )

    // Floating animation for background
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val fingerprintScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fingerprint_scale"
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
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(270.dp)
                .offset(x = (-65).dp, y = (130 + floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(60.dp)
        )

        Box(
            modifier = Modifier
                .size(230.dp)
                .offset(x = 215.dp, y = (470 - floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(55.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(32.dp)
                .verticalScroll(rememberScrollState())
                .alpha(contentAlpha)
                .offset(y = contentOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Animated fingerprint icon in glassmorphic container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(fingerprintScale)
                    .background(
                        color = GlassWhite10,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👆",
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Enable Biometric Lock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ChainWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Secure your messages with fingerprint\nor face recognition",
                style = MaterialTheme.typography.bodyLarge,
                color = ChainLightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Features in glassmorphic card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = GlassWhite10
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    BiometricFeature(
                        icon = "🔒",
                        title = "Extra Security",
                        description = "Additional protection for your account"
                    )

                    Divider(
                        color = GlassWhite10,
                        thickness = 1.dp
                    )

                    BiometricFeature(
                        icon = "⚡",
                        title = "Quick Access",
                        description = "Unlock instantly with your biometric"
                    )

                    Divider(
                        color = GlassWhite10,
                        thickness = 1.dp
                    )

                    BiometricFeature(
                        icon = "🛡️",
                        title = "Privacy Protection",
                        description = "Keep conversations private and secure"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Enable button
            ChainButton(
                text = if (state.isLoading) "Enabling..." else "Enable Biometric",
                onClick = viewModel::onEnableBiometricClick,
                enabled = !state.isLoading,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skip button
            ChainSecondaryButton(
                text = "Skip for Now",
                onClick = viewModel::onSkipClick,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You can always enable this later in Settings",
                style = MaterialTheme.typography.labelSmall,
                color = ChainMediumGray,
                textAlign = TextAlign.Center
            )
        }

        // Error snackbar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                containerColor = ChainError,
                contentColor = ChainWhite,
                shape = RoundedCornerShape(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = ChainWhite)
                    }
                }
            ) {
                Text(state.error!!)
            }
        }
    }
}

@Composable
private fun BiometricFeature(
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
