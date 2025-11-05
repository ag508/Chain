package com.chain.app.presentation.auth.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
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
import com.chain.app.presentation.components.ChainTextField
import com.chain.app.presentation.theme.*

@Composable
fun PhoneNumberScreen(
    onBackClick: () -> Unit,
    onOtpSent: (String) -> Unit,
    viewModel: PhoneNumberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    // When OTP is sent navigate
    LaunchedEffect(state.otpSent) {
        if (state.otpSent) onOtpSent("${state.countryCode}${state.phoneNumber}")
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
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    modifier = Modifier.size(36.dp),
                    tint = GlassText
                )
            }

            Spacer(Modifier.height(24.dp))

            // Auth title: 28sp bold
            Text(
                text = "Enter Your Phone",
                style = MaterialTheme.typography.displaySmall,
                color = GlassText
            )

            Spacer(Modifier.height(8.dp))

            // Auth subtitle: 15sp
            Text(
                text = "We'll send you a verification code to confirm your number",
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
            // Input group
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Country code field (simplified, glass input)
                ChainTextField(
                    value = state.countryCode,
                    onValueChange = viewModel::onCountryCodeChanged,
                    modifier = Modifier.width(100.dp),
                    placeholder = "+1",
                    textAlign = TextAlign.Center
                )

                // Phone number field
                ChainTextField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = "Phone number",
                    errorMessage = state.phoneNumberError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
            }

            // Continue button
            ChainButton(
                text = if (state.isLoading) "Sending..." else "Continue",
                onClick = viewModel::onContinueClick,
                enabled = state.phoneNumber.isNotBlank() && !state.isLoading,
                isAccent = true,
                isLoading = state.isLoading
            )
        }

        Spacer(Modifier.weight(1f))

        // Footer text
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy",
            style = MaterialTheme.typography.labelSmall,
            color = GlassTextMuted,
            textAlign = TextAlign.Center
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
