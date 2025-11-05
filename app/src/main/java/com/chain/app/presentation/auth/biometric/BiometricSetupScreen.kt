package com.chain.app.presentation.auth.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(modifier = Modifier.fillMaxSize().background(NeoDarkSurface).padding(24.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = NeoDarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Soft fingerprint icon
                Box(modifier = Modifier.size(92.dp).background(NeoDarkLightShadow.copy(alpha = 0.02f), RoundedCornerShape(46.dp)), contentAlignment = Alignment.Center) {
                    Text("🔒", style = MaterialTheme.typography.displaySmall)
                }

                Spacer(Modifier.height(16.dp))

                Text("Secure your messages instantly", style = MaterialTheme.typography.headlineSmall, color = NeoDarkTextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Enable biometrics for quick secure access to your account.", style = MaterialTheme.typography.bodyMedium, color = NeoDarkTextSecondary)

                Spacer(Modifier.height(20.dp))

                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = NeoDarkSurface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BiometricFeatureRow("Extra security", "Fingerprint or face unlock")
                        Divider(color = NeoDarkLightShadow.copy(alpha = 0.04f))
                        BiometricFeatureRow("Convenient", "Faster login without passwords")
                        Divider(color = NeoDarkLightShadow.copy(alpha = 0.04f))
                        BiometricFeatureRow("Optional", "You can enable later in Settings")
                    }
                }

                Spacer(Modifier.height(18.dp))

                ChainButton(
                    text = if (state.isLoading) "Enabling..." else "Enable Biometric",
                    onClick = viewModel::onEnableBiometricClick,
                    enabled = !state.isLoading,
                    isLoading = state.isLoading,
                    isAccent = true
                )

                Spacer(Modifier.height(12.dp))

                ChainSecondaryButton(text = "Skip for now", onClick = viewModel::onSkipClick, enabled = !state.isLoading)

                Spacer(Modifier.height(12.dp))
                Text("You can change this later in Settings", style = MaterialTheme.typography.labelSmall, color = NeoDarkTextSecondary)
            }
        }

        if (state.error != null) {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), containerColor = ChainError, contentColor = NeoDarkTextPrimary) {
                Text(state.error!!)
            }
        }
    }
}

@Composable
private fun BiometricFeatureRow(title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = NeoDarkTextPrimary)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = NeoDarkTextSecondary)
        }
    }
}
