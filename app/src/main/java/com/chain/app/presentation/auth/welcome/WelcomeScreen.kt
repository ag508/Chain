package com.chain.app.presentation.auth.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chain.app.R
import com.chain.app.presentation.components.ChainButton
import com.chain.app.presentation.components.ChainTextButton
import com.chain.app.presentation.theme.*

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surface = if (isDark) NeoDarkSurface else NeoLightSurface
    val lightShadow = if (isDark) NeoDarkLightShadow else NeoLightLightShadow
    val darkShadow = if (isDark) NeoDarkDarkShadow else NeoLightDarkShadow
    val textColor = if (isDark) NeoDarkTextPrimary else NeoLightTextPrimary
    val secondaryText = if (isDark) NeoDarkTextSecondary else NeoLightTextSecondary

    val zenDots = FontFamily(Font(resId = R.font.zendots_regular))
    val scrollState = rememberScrollState()
    val infinite = rememberInfiniteTransition(label = "bg_anim")

    // Background shimmer animation
    val offset by infinite.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset_anim"
    )

    // Animated gradient
    val bgGradient = Brush.linearGradient(
        listOf(
            ChainSecureGreen.copy(alpha = 0.3f),
            ChainCyan.copy(alpha = 0.25f),
            ChainSecureGreenLight.copy(alpha = 0.3f)
        ),
        start = Offset(0f, offset),
        end = Offset(offset, 0f)
    )

    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // HERO SECTION — logo + tagline
            HeroSection(surface, lightShadow, darkShadow, zenDots, textColor, secondaryText)

            Spacer(modifier = Modifier.height(40.dp))

            // FEATURES CAROUSEL — modern horizontal layout
            FeatureCarousel(textColor, secondaryText)

            Spacer(modifier = Modifier.height(48.dp))

            // CALL TO ACTION CARD
            ActionCard(onGetStartedClick, onSignInClick, secondaryText)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeroSection(
    surface: Color,
    lightShadow: Color,
    darkShadow: Color,
    zenDots: FontFamily,
    textColor: Color,
    secondaryText: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_logo_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_anim"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(surface)
                .neomorphicExtruded(lightShadow, darkShadow),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Chain Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chain",
            fontFamily = zenDots,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.displaySmall,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Decentralized Messaging, Reinvented.",
            style = MaterialTheme.typography.titleMedium,
            color = secondaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeatureCarousel(textColor: Color, secondaryColor: Color) {
    val features = listOf(
        Triple("🔐", "Total Privacy", "End-to-end encryption with blockchain key validation."),
        Triple("🌍", "Peer-to-Peer", "No central server, no censorship. Just you and your network."),
        Triple("⚡", "Blazing Fast", "Real-time message sync using distributed nodes."),
        Triple("🧩", "Modular", "Plug-in features for groups, wallets, and DAOs.")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        features.forEach { (icon, title, desc) ->
            FeatureCard(icon, title, desc, textColor, secondaryColor)
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    textColor: Color,
    secondaryColor: Color
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.05f),
                            ChainSecureGreen.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(icon, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = secondaryColor)
            }
        }
    }
}

@Composable
private fun ActionCard(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    secondaryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ChainButton(
                text = "Get Started",
                onClick = onGetStarted,
                isAccent = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already on Chain?", color = secondaryColor)
                Spacer(modifier = Modifier.width(4.dp))
                ChainTextButton("Sign In", onClick = onSignIn)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "By continuing, you agree to our Terms & Privacy Policy.",
                style = MaterialTheme.typography.labelSmall,
                color = secondaryColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
