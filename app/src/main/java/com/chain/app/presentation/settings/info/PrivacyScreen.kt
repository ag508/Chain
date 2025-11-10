package com.chain.app.presentation.settings.info

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chain.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
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
            .gradientBackground()
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
                    .glassCard(),
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
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = ChainSecureGreen,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Privacy Policy",
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

            // Privacy Commitment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ChainSecureGreen,
                        modifier = Modifier.size(28.dp)
                    )

                    Text(
                        text = "Your privacy is our priority. Chain uses end-to-end encryption to ensure only you and your recipients can read your messages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassText,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                    )
                }
            }

            // Privacy Sections
            PrivacySection(
                title = "1. Information We Collect",
                content = "We collect minimal information necessary to provide our service:\n\n" +
                        "• Account Information: Phone number, display name, and profile picture\n" +
                        "• Usage Data: Device information, IP address, and usage statistics\n" +
                        "• Messages: Temporarily stored in encrypted form until delivered\n" +
                        "• Contacts: Only to facilitate communication (if you grant permission)"
            )

            PrivacySection(
                title = "2. How We Use Your Information",
                content = "We use collected information to:\n\n" +
                        "• Provide and maintain the Chain service\n" +
                        "• Improve user experience and app functionality\n" +
                        "• Send important service notifications\n" +
                        "• Prevent fraud and ensure security\n" +
                        "• Comply with legal obligations"
            )

            PrivacySection(
                title = "3. End-to-End Encryption",
                content = "All messages, calls, photos, videos, and files you share are protected by end-to-end encryption. This means:\n\n" +
                        "• Only you and your recipients can access your content\n" +
                        "• Chain cannot read your messages or listen to your calls\n" +
                        "• Your communications remain private and secure\n" +
                        "• Messages are automatically deleted from our servers once delivered"
            )

            PrivacySection(
                title = "4. Data Storage",
                content = "• Messages are temporarily stored on our servers in encrypted form until delivery\n" +
                        "• Local backups are stored on your device\n" +
                        "• Cloud backups (if enabled) are encrypted\n" +
                        "• Media files are stored locally on your device\n" +
                        "• Account information is stored securely on our servers"
            )

            PrivacySection(
                title = "5. Third-Party Services",
                content = "Chain does not sell, trade, or rent your personal information to third parties. We may use trusted third-party services for:\n\n" +
                        "• Message delivery infrastructure\n" +
                        "• Analytics (anonymized data only)\n" +
                        "• Payment processing (for premium features)\n\n" +
                        "These services are bound by strict confidentiality agreements."
            )

            PrivacySection(
                title = "6. Data Sharing",
                content = "We may share your information only when:\n\n" +
                        "• Required by law or legal process\n" +
                        "• Necessary to protect our rights or safety\n" +
                        "• You explicitly consent to sharing\n" +
                        "• Part of a business transfer (with your notification)\n\n" +
                        "We will never share your message content or communications."
            )

            PrivacySection(
                title = "7. Your Rights",
                content = "You have the right to:\n\n" +
                        "• Access your personal data\n" +
                        "• Correct inaccurate information\n" +
                        "• Delete your account and associated data\n" +
                        "• Export your data\n" +
                        "• Opt-out of non-essential data collection\n" +
                        "• Control who can see your information"
            )

            PrivacySection(
                title = "8. Children's Privacy",
                content = "Chain is not intended for users under 16 years of age. We do not knowingly collect information from children. If we learn we have collected information from a child under 16, we will delete it immediately."
            )

            PrivacySection(
                title = "9. Data Retention",
                content = "• Account data: Retained while your account is active\n" +
                        "• Messages: Deleted from servers after delivery\n" +
                        "• Usage data: Retained for up to 90 days\n" +
                        "• Deleted accounts: All data permanently deleted within 30 days"
            )

            PrivacySection(
                title = "10. Security",
                content = "We implement industry-standard security measures:\n\n" +
                        "• End-to-end encryption for all communications\n" +
                        "• Encrypted data storage\n" +
                        "• Regular security audits\n" +
                        "• Two-factor authentication support\n" +
                        "• Secure server infrastructure"
            )

            PrivacySection(
                title = "11. International Users",
                content = "Chain is a global service. Your information may be transferred to and processed in countries other than your own. We ensure appropriate safeguards are in place to protect your data regardless of location."
            )

            PrivacySection(
                title = "12. Changes to Privacy Policy",
                content = "We may update this Privacy Policy from time to time. We will notify you of any significant changes via the app or email. Continued use of Chain after changes constitutes acceptance of the updated policy."
            )

            PrivacySection(
                title = "13. Contact Us",
                content = "If you have questions about this Privacy Policy or how we handle your data, please contact us at:\n\n" +
                        "Email: privacy@chain-app.com\n" +
                        "Website: www.chain-app.com/privacy"
            )

            // Footer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ChainSecureGreen,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "Your privacy is protected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ChainSecureGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "© 2024 Chain App • All rights reserved",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(),
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
