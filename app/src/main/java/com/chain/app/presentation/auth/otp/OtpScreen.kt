package com.chain.app.presentation.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*

@Composable
fun OtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onVerified: (String) -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    LaunchedEffect(state.verified) {
        if (state.verified && state.userId != null) onVerified(state.userId!!)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { i ->
                    val char = if (i < otp.length) otp[i].toString() else ""
                    val isFocused = i == otp.length

                    // OTP box: 56x64dp with 16dp border radius, glass effect
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(64.dp)
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
