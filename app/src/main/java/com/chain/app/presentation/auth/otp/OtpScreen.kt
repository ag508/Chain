package com.chain.app.presentation.auth.otp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
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

/**
 * OTP verification screen with premium PIN input design and glassmorphism.
 * Features animated PIN cells and gradient background for a modern look.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onVerified: (String) -> Unit,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate when verified
    LaunchedEffect(state.verified) {
        if (state.verified && state.userId != null) {
            onVerified(state.userId!!)
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

    // Floating animation for background
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
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
                .size(280.dp)
                .offset(x = (-70).dp, y = (150 + floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(55.dp)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 200.dp, y = (450 - floatOffset).dp)
                .background(
                    color = GlassWhite5,
                    shape = CircleShape
                )
                .blur(55.dp)
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
                    text = "Verification",
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
                    .alpha(contentAlpha)
                    .offset(y = contentOffset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Security icon in glassmorphic container
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
                        text = "🔐",
                        style = MaterialTheme.typography.displayMedium
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Enter verification code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChainWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We've sent a 6-digit code to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChainLightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ChainWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                // OTP Input - Premium PIN style
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = GlassWhite10
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Verification Code",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChainWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OtpInput(
                            otp = state.otp,
                            onOtpChange = viewModel::onOtpChanged,
                            isError = state.otpError != null
                        )

                        if (state.otpError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.otpError!!,
                                color = ChainError,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Resend code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Didn't receive the code?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChainLightGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ChainTextButton(
                        text = "Resend",
                        onClick = viewModel::onResendClick,
                        enabled = !state.isLoading
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))

                // Verify button
                ChainButton(
                    text = if (state.isLoading) "Verifying..." else "Verify",
                    onClick = viewModel::onVerifyClick,
                    enabled = state.otp.length == 6 && !state.isLoading,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))
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

@Composable
private fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean = false
) {
    val otpLength = 6

    BasicTextField(
        value = TextFieldValue(otp, selection = TextRange(otp.length)),
        onValueChange = {
            if (it.text.length <= otpLength && it.text.all { char -> char.isDigit() }) {
                onOtpChange(it.text)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpLength) { index ->
                    val char = when {
                        index < otp.length -> otp[index].toString()
                        else -> ""
                    }

                    val isFocused = index == otp.length

                    OtpCell(
                        char = char,
                        isFocused = isFocused,
                        isError = isError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpCell(
    char: String,
    isFocused: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    // Animated scale for focus state
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cell_scale"
    )

    val borderColor = when {
        isError -> ChainError
        isFocused -> ChainAccent
        char.isNotEmpty() -> ChainWhite.copy(alpha = 0.5f)
        else -> GlassWhite20
    }

    val backgroundColor = when {
        isFocused -> ChainAccent.copy(alpha = 0.15f)
        char.isNotEmpty() -> GlassWhite20
        else -> GlassWhite10
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(
                width = if (isFocused) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing cursor for focused empty cell
        if (isFocused && char.isEmpty()) {
            val cursorAlpha by rememberInfiniteTransition(label = "cursor_transition")
                .animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cursor_alpha"
                )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .alpha(cursorAlpha)
                    .background(ChainAccent, RoundedCornerShape(1.dp))
            )
        }

        // Display character when present
        if (char.isNotEmpty()) {
            Text(
                text = char,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isError) ChainError else ChainWhite
            )
        }
    }
}
