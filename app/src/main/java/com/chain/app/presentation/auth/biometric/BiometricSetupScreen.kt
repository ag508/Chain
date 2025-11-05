package com.chain.app.presentation.auth.biometric

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainSecondaryButton
import com.chain.app.presentation.theme.*

@Composable
fun BiometricSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: BiometricSetupViewModel = hiltViewModel()
) {
    println("DEBUG BiometricScreen: Screen composing")
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        println("DEBUG BiometricScreen: LaunchedEffect initialized")
    }

    LaunchedEffect(state.setupComplete) {
        println("DEBUG BiometricScreen: setupComplete = ${state.setupComplete}")
        if (state.setupComplete) {
            println("DEBUG BiometricScreen: Calling onSetupComplete()")
            onSetupComplete()
        }
    }

    // Gradient background (135deg from HTML spec)
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    // Pulse animation for biometric icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Biometric content (centered)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Biometric icon: 140x140dp with 28dp border radius, pulse animation
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .glassBiometricIcon(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric",
                    modifier = Modifier.size(64.dp),
                    tint = GlassText
                )
            }

            Spacer(Modifier.height(32.dp))

            // Biometric text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Biometric title: 24sp bold
                Text(
                    text = "Enable Biometric",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GlassText
                )

                Spacer(Modifier.height(12.dp))

                // Biometric subtitle: 15sp
                Text(
                    text = "Use your fingerprint or face to unlock Chain securely and quickly",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Buttons at bottom
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChainButton(
                text = if (state.isLoading) "Enabling..." else "Enable Biometric",
                onClick = viewModel::onEnableBiometricClick,
                enabled = !state.isLoading,
                isLoading = state.isLoading,
                isAccent = true
            )

            ChainSecondaryButton(
                text = "Maybe Later",
                onClick = viewModel::onSkipClick,
                enabled = !state.isLoading
            )
        }

        // Error snackbar
        if (state.error != null) {
            Spacer(Modifier.height(16.dp))
            Snackbar(
                containerColor = ChainError,
                contentColor = GlassText
            ) {
                Text(state.error!!)
            }
        }
    }
}
