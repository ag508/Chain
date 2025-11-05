package com.chain.app.presentation.auth.otp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onVerified: (String) -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.verified) {
        if (state.verified && state.userId != null) onVerified(state.userId!!)
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoDarkSurface)
            .padding(24.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeoDarkTextPrimary)
            }
            Text("Verification", style = MaterialTheme.typography.titleMedium, color = NeoDarkTextPrimary)
        }

        // Center card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = NeoDarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Verify your number", style = MaterialTheme.typography.headlineSmall, color = NeoDarkTextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("Enter the 6-digit code sent to", style = MaterialTheme.typography.bodyMedium, color = NeoDarkTextSecondary)
                Text(phoneNumber, style = MaterialTheme.typography.bodyLarge, color = NeoDarkTextPrimary)
                Spacer(Modifier.height(18.dp))

                // OTP input: simple matte cells
                OtpInput(
                    otp = state.otp,
                    onOtpChange = viewModel::onOtpChanged,
                    isError = state.otpError != null
                )

                if (state.otpError != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(state.otpError!!, color = ChainError, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.Center) {
                    Text("Didn't receive it?", color = NeoDarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    ChainTextButton(text = "Resend", onClick = viewModel::onResendClick, enabled = !state.isLoading)
                }

                Spacer(Modifier.height(14.dp))

                ChainButton(
                    text = if (state.isLoading) "Verifying..." else "Verify",
                    onClick = viewModel::onVerifyClick,
                    enabled = state.otp.length == 6 && !state.isLoading,
                    isLoading = state.isLoading,
                    isAccent = true
                )
            }
        }

        // Error snackbar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = ChainError,
                contentColor = NeoDarkTextPrimary
            ) { Text(state.error!!) }
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(length) { i ->
                    val char = if (i < otp.length) otp[i].toString() else ""
                    val isFocused = i == otp.length
                    val scale by animateFloatAsState(targetValue = if (isFocused) 1.02f else 1f, animationSpec = spring())
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (char.isNotEmpty()) NeoDarkLightShadow.copy(alpha = 0.03f) else NeoDarkSurface,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (char.isNotEmpty()) {
                                Text(char, style = MaterialTheme.typography.headlineSmall, color = if (isError) ChainError else NeoDarkTextPrimary)
                            } else if (isFocused) {
                                // subtle cursor line (matte)
                                Box(Modifier.width(2.dp).height(18.dp).background(NeoDarkTextSecondary))
                            }
                        }
                    }
                }
            }
        }
    )
}
