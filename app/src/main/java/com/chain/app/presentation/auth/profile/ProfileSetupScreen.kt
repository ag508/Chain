package com.chain.app.presentation.auth.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainSecondaryButton
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

@Composable
fun ProfileSetupScreen(
    userId: String,
    phoneNumber: String,
    onProfileCreated: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        println("DEBUG ProfileScreen: Initializing with userId=$userId, phoneNumber=$phoneNumber")
        viewModel.setUserData(userId, phoneNumber)
    }

    LaunchedEffect(state.profileCreated) {
        println("DEBUG ProfileScreen: profileCreated changed to ${state.profileCreated}")
        if (state.profileCreated) {
            println("DEBUG ProfileScreen: Calling onProfileCreated() navigation callback")
            onProfileCreated()
        }
    }

    // Gradient background (135deg from HTML spec)
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Auth header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            // Auth icon: 80x80dp with 20dp border radius, glass effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .glassAuthIcon(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(36.dp),
                    tint = GlassText
                )
            }

            Spacer(Modifier.height(24.dp))

            // Auth title: 28sp bold
            Text(
                text = "Display Name",
                style = MaterialTheme.typography.displaySmall,
                color = GlassText
            )

            Spacer(Modifier.height(8.dp))

            // Auth subtitle: 15sp
            Text(
                text = "Choose a name that your contacts will see",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Auth form
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display name input
            ChainTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = "satoshi",
                errorMessage = state.nameError
            )

            // Continue button
            ChainButton(
                text = if (state.isLoading) "Creating..." else "Continue",
                onClick = viewModel::onContinueClick,
                enabled = state.name.isNotBlank() && !state.isLoading,
                isLoading = state.isLoading,
                isAccent = true
            )
        }

        Spacer(Modifier.weight(1f))

        // Skip button
        ChainSecondaryButton(
            text = "Skip for Now",
            onClick = viewModel::onContinueClick,
            enabled = !state.isLoading
        )

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
