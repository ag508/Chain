package com.chain.app.presentation.auth.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainSecondaryButton
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

@Composable
fun ProfileSetupScreen(
    userId: String,
    phoneNumber: String,
    email: String,
    onProfileCreated: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onProfileImageSelected(uri)
    }

    LaunchedEffect(Unit) {
        println("DEBUG ProfileScreen: Initializing with userId=$userId, phoneNumber=$phoneNumber, email=$email")
        viewModel.setUserData(userId, phoneNumber, email)
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
            .systemBarsPadding()
            .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Auth header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            // Profile photo picker (optional)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(2.dp, GlassBorder, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.profileImageUri != null) {
                    AsyncImage(
                        model = state.profileImageUri,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(36.dp),
                            tint = GlassTextSecondary
                        )
                        Text(
                            text = "Optional",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Auth title: 28sp bold
            Text(
                text = "Set Up Profile",
                style = MaterialTheme.typography.displaySmall,
                color = GlassText
            )

            Spacer(Modifier.height(8.dp))

            // Auth subtitle: 15sp
            Text(
                text = "Choose a name and photo (optional)",
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
                placeholder = "Display Name",
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

            // Skip button (only shown when name is filled)
            if (state.name.isNotBlank()) {
                ChainSecondaryButton(
                    text = "Skip Photo for Now",
                    onClick = viewModel::onContinueClick,
                    enabled = !state.isLoading
                )
            }
        }

        Spacer(Modifier.weight(1f))

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
