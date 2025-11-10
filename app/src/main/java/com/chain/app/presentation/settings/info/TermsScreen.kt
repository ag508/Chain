package com.chain.app.presentation.settings.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms of Service",
                        color = GlassText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.glass()
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GlassGradientStart, GlassGradientEnd)
                )
            )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = GlassAccent,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Terms of Service",
                            style = MaterialTheme.typography.titleLarge,
                            color = GlassText,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Last updated: January 2024",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            // Terms Content
            TermsSection(
                title = "1. Acceptance of Terms",
                content = "By accessing and using Chain, you accept and agree to be bound by the terms and provision of this agreement. If you do not agree to these terms, please do not use our service."
            )

            TermsSection(
                title = "2. Use of Service",
                content = "You must be at least 16 years old to use Chain. You are responsible for maintaining the confidentiality of your account and password. You agree to accept responsibility for all activities that occur under your account."
            )

            TermsSection(
                title = "3. User Conduct",
                content = "You agree not to use Chain to:\n" +
                        "• Violate any laws or regulations\n" +
                        "• Transmit harmful, threatening, or abusive content\n" +
                        "• Harass or harm other users\n" +
                        "• Impersonate any person or entity\n" +
                        "• Distribute spam or unsolicited messages\n" +
                        "• Attempt to gain unauthorized access to the service"
            )

            TermsSection(
                title = "4. Intellectual Property",
                content = "The service and its original content, features, and functionality are owned by Chain and are protected by international copyright, trademark, patent, trade secret, and other intellectual property laws."
            )

            TermsSection(
                title = "5. Privacy",
                content = "Your use of Chain is also governed by our Privacy Policy. Please review our Privacy Policy to understand our practices regarding your personal information."
            )

            TermsSection(
                title = "6. Content",
                content = "You retain all rights to the content you submit, post, or display on or through Chain. By submitting content, you grant us a worldwide, non-exclusive license to use, reproduce, and distribute your content as necessary to provide the service."
            )

            TermsSection(
                title = "7. Termination",
                content = "We may terminate or suspend your account and access to the service immediately, without prior notice, for conduct that we believe violates these Terms of Service or is harmful to other users, us, or third parties."
            )

            TermsSection(
                title = "8. Disclaimer",
                content = "Chain is provided \"as is\" and \"as available\" without any warranties of any kind. We do not guarantee that the service will be uninterrupted, timely, secure, or error-free."
            )

            TermsSection(
                title = "9. Limitation of Liability",
                content = "In no event shall Chain be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of or inability to use the service."
            )

            TermsSection(
                title = "10. Changes to Terms",
                content = "We reserve the right to modify these terms at any time. We will notify users of any material changes. Your continued use of Chain after such modifications constitutes acceptance of the updated terms."
            )

            TermsSection(
                title = "11. Contact",
                content = "If you have any questions about these Terms, please contact us at legal@chain-app.com"
            )

            // Footer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "© 2024 Chain App",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )

                    Text(
                        text = "All rights reserved",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = GlassText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTextSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
        }
    }
}
