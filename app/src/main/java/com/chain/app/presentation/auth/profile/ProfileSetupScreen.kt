package com.chain.app.presentation.auth.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

/**
 * Profile setup screen with modern glassmorphic design and image picker.
 * Features gradient background and elegant profile creation interface.
 */
@Composable
fun ProfileSetupScreen(
    userId: String,
    phoneNumber: String,
    onProfileCreated: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Set user data on first composition
    LaunchedEffect(Unit) {
        viewModel.setUserData(userId, phoneNumber)
    }

    // Navigate when profile is created
    LaunchedEffect(state.profileCreated) {
        if (state.profileCreated) {
            onProfileCreated()
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onProfileImageSelected(uri)
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
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
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
                .size(260.dp)
                .offset(x = (-60).dp, y = (140 + floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(58.dp)
        )

        Box(
            modifier = Modifier
                .size(210.dp)
                .offset(x = 210.dp, y = (480 - floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(52.dp)
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
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Create your profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ChainWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Add your name and a profile photo",
                style = MaterialTheme.typography.bodyLarge,
                color = ChainLightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Profile image picker with glassmorphic design
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(3.dp, GlassWhite20, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.profileImageUri != null) {
                    AsyncImage(
                        model = state.profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Edit overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = GlassBlack30,
                                shape = CircleShape
                            )
                    )

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(32.dp),
                        tint = ChainWhite
                    )
                } else {
                    // Empty state with glassmorphic background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = GlassWhite10,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "Add Photo",
                                style = MaterialTheme.typography.labelMedium,
                                color = ChainLightGray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Camera icon badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp)
                            .background(ChainAccent, CircleShape)
                            .border(3.dp, GradientDarkStart, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(20.dp),
                            tint = ChainWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Name input in glassmorphic card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = GlassWhite10
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Display Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = ChainWhite,
                        fontWeight = FontWeight.SemiBold
                    )

                    ChainTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        label = "Name",
                        placeholder = "Enter your name",
                        errorMessage = state.nameError,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = ChainLightGray
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.onContinueClick()
                            }
                        )
                    )

                    Text(
                        text = "This is the name that will be visible to your contacts",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChainMediumGray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Continue button
            ChainButton(
                text = if (state.isLoading) "Creating Profile..." else "Continue",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onContinueClick()
                },
                enabled = state.name.isNotBlank() && !state.isLoading,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
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
