package com.chain.app.presentation.auth.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.chain.app.presentation.components.ChainButton
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

    LaunchedEffect(Unit) { viewModel.setUserData(userId, phoneNumber) }
    LaunchedEffect(state.profileCreated) { if (state.profileCreated) onProfileCreated() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onProfileImageSelected(uri)
    }

    Box(modifier = Modifier.fillMaxSize().background(NeoDarkSurface).padding(24.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NeoDarkSurface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 12.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(88.dp).clickable { imagePicker.launch("image/*") }) {
                    if (state.profileImageUri != null) {
                        AsyncImage(
                            model = state.profileImageUri,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(88.dp)
                                .border(2.dp, Color.Transparent, CircleShape)
                                .background(NeoDarkSurface, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(NeoDarkLightShadow.copy(alpha = 0.02f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Tap to add photo (optional)", style = MaterialTheme.typography.labelMedium, color = NeoDarkTextSecondary)

                Spacer(Modifier.height(16.dp))
                Text("Set up your Chain identity", style = MaterialTheme.typography.headlineSmall, color = NeoDarkTextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("This name will be shown to your contacts", style = MaterialTheme.typography.bodySmall, color = NeoDarkTextSecondary)

                Spacer(Modifier.height(18.dp))

                ChainTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChanged,
                    label = "Display name",
                    placeholder = "Satoshi Nakamoto",
                    errorMessage = state.nameError
                )

                Spacer(Modifier.height(18.dp))

                ChainButton(
                    text = if (state.isLoading) "Creating..." else "Continue",
                    onClick = viewModel::onContinueClick,
                    enabled = state.name.isNotBlank() && !state.isLoading,
                    isLoading = state.isLoading,
                    isAccent = true
                )
            }
        }

        if (state.error != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = ChainError,
                contentColor = NeoDarkTextPrimary
            ) {
                Text(state.error!!)
            }
        }
    }
}
