package com.chain.app.presentation.auth.phone

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

/**
 * Phone number entry screen with modern minimalist design and glassmorphism.
 * Features gradient background and clean, spacious layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberScreen(
    onBackClick: () -> Unit,
    onOtpSent: (String) -> Unit,
    viewModel: PhoneNumberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Navigate to OTP screen when OTP is sent
    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            onOtpSent("${state.countryCode}${state.phoneNumber}")
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

    // Floating animation for background elements
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
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
                .size(250.dp)
                .offset(x = (-80).dp, y = (120 + floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(60.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 220.dp, y = (500 - floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(50.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Custom top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ChainWhite
                    )
                }
                Text(
                    text = "Verify Phone",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ChainWhite,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
                    .alpha(contentAlpha)
                    .offset(y = contentOffset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Phone icon in glassmorphic container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = GlassWhite10,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.displayMedium
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Enter your phone number",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChainWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We'll send you a verification code via SMS",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ChainLightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                // Input card with glassmorphism
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = GlassWhite10
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChainWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Phone number input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Country code
                            OutlinedTextField(
                                value = state.countryCode,
                                onValueChange = viewModel::onCountryCodeChanged,
                                modifier = Modifier.width(100.dp),
                                label = { Text("Code", color = ChainLightGray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ChainWhite,
                                    unfocusedTextColor = ChainWhite,
                                    focusedBorderColor = ChainAccent,
                                    unfocusedBorderColor = GlassWhite20,
                                    cursorColor = ChainAccent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            // Phone number
                            ChainTextField(
                                value = state.phoneNumber,
                                onValueChange = viewModel::onPhoneNumberChanged,
                                modifier = Modifier.weight(1f),
                                label = "Number",
                                placeholder = "1234567890",
                                errorMessage = state.phoneNumberError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.onContinueClick()
                                    }
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))

                // Continue button
                ChainButton(
                    text = if (state.isLoading) "Sending..." else "Continue",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onContinueClick()
                    },
                    enabled = state.phoneNumber.isNotBlank() && !state.isLoading,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy notice
                Text(
                    text = "By providing your phone number, you agree to receive\nverification codes from Chain",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChainMediumGray,
                    textAlign = TextAlign.Center
                )
            }
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
