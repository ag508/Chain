package com.chain.app.presentation.auth.phone

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberScreen(
    onBackClick: () -> Unit,
    onOtpSent: (String) -> Unit,
    viewModel: PhoneNumberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val zenDots = FontFamily.Default // Replace with FontFamily(Font(R.font.zendots_regular)) if available

    // When OTP is sent navigate
    LaunchedEffect(state.otpSent) {
        if (state.otpSent) onOtpSent("${state.countryCode}${state.phoneNumber}")
    }

    // Subtle entrance animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Matte background gradient (very subtle)
    val bgBrush = Brush.verticalGradient(
        colors = listOf(NeoDarkSurface, NeoDarkSurface.copy(alpha = 0.98f))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .padding(24.dp)
    ) {
        // Top app bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeoDarkTextPrimary)
            }
            Text(
                text = "Verify Phone",
                style = MaterialTheme.typography.titleMedium,
                color = NeoDarkTextPrimary
            )
        }

        // Centered content card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card container
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = NeoDarkSurface,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scroll)
                        .alpha(if (visible) 1f else 0f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // small, restrained icon with soft pulse
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .shadow(2.dp, RoundedCornerShape(44.dp))
                            .background(NeoDarkSurface, RoundedCornerShape(44.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", style = MaterialTheme.typography.displaySmall)
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Let's get you connected",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NeoDarkTextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Enter your phone number to receive a verification code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoDarkTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Input group card (matte)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeoDarkSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Phone Number", color = NeoDarkTextSecondary, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.countryCode,
                                    onValueChange = viewModel::onCountryCodeChanged,
                                    singleLine = true,
                                    modifier = Modifier.width(100.dp),
                                    label = { Text("Code", color = NeoDarkTextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ChainSecureGreen,
                                        unfocusedBorderColor = NeoDarkTextSecondary,
                                        focusedTextColor = NeoDarkTextPrimary,
                                        unfocusedTextColor = NeoDarkTextPrimary,
                                        cursorColor = ChainSecureGreen
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                ChainTextField(
                                    value = state.phoneNumber,
                                    onValueChange = viewModel::onPhoneNumberChanged,
                                    modifier = Modifier.weight(1f),
                                    label = "Number",
                                    placeholder = "1234567890",
                                    errorMessage = state.phoneNumberError,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    ChainButton(
                        text = if (state.isLoading) "Sending..." else "Send Code",
                        onClick = { viewModel.onContinueClick() },
                        enabled = state.phoneNumber.isNotBlank() && !state.isLoading,
                        isAccent = true,
                        isLoading = state.isLoading
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "By providing your number, you agree to receive verification messages from Chain.",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoDarkTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Error snackbar (keeps previous behavior)
        if (state.error != null) {
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180))
            ) {
                Snackbar(
                    containerColor = ChainError,
                    contentColor = NeoDarkTextPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                ) { Text(state.error!!) }
            }
        }
    }
}
