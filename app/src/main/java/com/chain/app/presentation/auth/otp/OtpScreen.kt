package com.chain.app.presentation.auth.otp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onVerified: (String) -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    // Track popup states
    var showSuccessPopup by remember { mutableStateOf(false) }
    var showErrorPopup by remember { mutableStateOf(false) }

    // Show success popup and delay navigation
    LaunchedEffect(state.verified) {
        if (state.verified && state.userId != null) {
            showSuccessPopup = true
            delay(1500) // Show success popup for 1.5 seconds
            onVerified(state.userId!!)
        }
    }

    // Show error popup when verification fails
    LaunchedEffect(state.otpError) {
        if (state.otpError != null) {
            showErrorPopup = true
            delay(3000) // Show error popup for 3 seconds
            showErrorPopup = false
        }
    }

    // Gradient background (135deg from HTML spec)
    val bgBrush = Brush.linearGradient(
        colors = listOf(GlassGradientStart, GlassGradientEnd)
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
            // Auth icon: 80x80dp with 20dp border radius, glass effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .glassAuthIcon(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(36.dp),
                    tint = GlassText
                )
            }

            Spacer(Modifier.height(24.dp))

            // Auth title: 28sp bold
            Text(
                text = "Verify Code",
                style = MaterialTheme.typography.displaySmall,
                color = GlassText
            )

            Spacer(Modifier.height(8.dp))

            // Auth subtitle: 15sp
            Text(
                text = "Enter the 6-digit code we sent to\n$phoneNumber",
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
            // OTP boxes: 56x64dp with 16dp border radius
            OtpInput(
                otp = state.otp,
                onOtpChange = viewModel::onOtpChanged,
                isError = state.otpError != null
            )

            if (state.otpError != null) {
                Text(
                    state.otpError!!,
                    color = ChainError,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Resend section
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Didn't receive the code?",
                    color = GlassTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(6.dp))
                ChainTextButton(
                    text = "Resend Code",
                    onClick = viewModel::onResendClick,
                    enabled = !state.isLoading
                )
            }

            // Verify button
            ChainButton(
                text = if (state.isLoading) "Verifying..." else "Verify & Continue",
                onClick = viewModel::onVerifyClick,
                enabled = state.otp.length == 6 && !state.isLoading,
                isLoading = state.isLoading,
                isAccent = true
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

        // Verification Success Popup
        AnimatedVisibility(
            visible = showSuccessPopup,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            VerificationResultPopup(
                isSuccess = true,
                message = "Verification Successful!",
                description = "Redirecting to profile setup..."
            )
        }

        // Verification Error Popup
        AnimatedVisibility(
            visible = showErrorPopup,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            VerificationResultPopup(
                isSuccess = false,
                message = "Verification Failed",
                description = state.otpError ?: "Invalid code. Please try again."
            )
        }
    }
}

@Composable
private fun OtpInput(otp: String, onOtpChange: (String) -> Unit, isError: Boolean) {
    val length = 6
    BasicTextField(
        value = TextFieldValue(otp, selection = TextRange(otp.length)),
        onValueChange = {
            if (it.text.length <= length && it.text.all(Char::isDigit)) onOtpChange(it.text)
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { i ->
                    val char = if (i < otp.length) otp[i].toString() else ""
                    val isFocused = i == otp.length

                    // OTP box: flexible width with 64dp height, 16dp border radius, glass effect
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.875f)  // 56:64 ratio = 0.875
                            .glassOtpBox(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char.isNotEmpty()) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.displaySmall,
                                color = if (isError) ChainError else GlassText
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun VerificationResultPopup(
    isSuccess: Boolean,
    message: String,
    description: String
) {
    // Semi-transparent overlay with blur effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassOverlay),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic popup card with strong blur effect
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(GlassSurface)  // Stronger glass background for more blur effect
                .border(width = 1.dp, color = GlassBorder, shape = RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSuccess) ChainSuccess.copy(alpha = 0.2f) else ChainError.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                    contentDescription = if (isSuccess) "Success" else "Error",
                    modifier = Modifier.size(48.dp),
                    tint = if (isSuccess) ChainSuccess else ChainError
                )
            }

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = GlassText,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
