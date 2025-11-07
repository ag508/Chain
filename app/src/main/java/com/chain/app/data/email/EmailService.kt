package com.chain.app.data.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Service for sending emails via SMTP.
 * Configured for Kauzway mail server.
 */
@Singleton
class EmailService @Inject constructor() {

    // SMTP Configuration for Kauzway
    private val smtpHost = "mail.kauzway.com"
    private val smtpPort = "465"
    private val username = "chain.auth@kauzway.com"
    private val password = "Chain@123"

    /**
     * Send OTP email to the specified email address.
     * @param toEmail Recipient's email address
     * @param otp OTP code to send
     * @return Result indicating success or failure
     */
    suspend fun sendOtpEmail(toEmail: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Configure SMTP properties
            val props = Properties().apply {
                put("mail.smtp.host", smtpHost)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.auth", "true")
                put("mail.smtp.socketFactory.port", smtpPort)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
                put("mail.smtp.ssl.enable", "true")
            }

            // Create session with authentication
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            // Create message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username, "Chain Secure Messaging"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = "Your Chain Verification Code"
                setContent(buildHtmlEmail(otp), "text/html; charset=utf-8")
            }

            // Send message
            Transport.send(message)

            Timber.d("OTP email sent successfully to $toEmail")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send OTP email to $toEmail")
            Result.failure(e)
        }
    }

    /**
     * Build HTML email template with logo and styling.
     */
    private fun buildHtmlEmail(otp: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link href="https://fonts.googleapis.com/css2?family=Zen+Dots&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Inter', Arial, sans-serif;
                        background: linear-gradient(135deg, #1A1A1A 0%, #000000 100%);
                        color: #FFFFFF;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 40px 20px;
                    }
                    .logo-container {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        width: 120px;
                        height: 120px;
                        border-radius: 30px;
                        background: rgba(255, 255, 255, 0.05);
                        backdrop-filter: blur(10px);
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        padding: 10px;
                        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                    }
                    .brand-name {
                        font-family: 'Zen Dots', cursive;
                        font-size: 42px;
                        font-weight: 800;
                        color: #FFFFFF;
                        margin: 20px 0 10px 0;
                        text-align: center;
                    }
                    .content {
                        background: rgba(30, 30, 30, 0.6);
                        backdrop-filter: blur(10px);
                        border-radius: 20px;
                        padding: 40px;
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                    }
                    .welcome-text {
                        font-size: 24px;
                        font-weight: 600;
                        color: #FFFFFF;
                        margin-bottom: 20px;
                        text-align: center;
                    }
                    .message {
                        font-size: 16px;
                        line-height: 1.6;
                        color: #B3B3B3;
                        margin-bottom: 30px;
                        text-align: center;
                    }
                    .otp-container {
                        background: rgba(255, 255, 255, 0.05);
                        border-radius: 16px;
                        padding: 30px;
                        text-align: center;
                        margin: 30px 0;
                        border: 1px solid rgba(255, 255, 255, 0.1);
                    }
                    .otp-label {
                        font-size: 14px;
                        color: #B3B3B3;
                        margin-bottom: 10px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .otp-code {
                        font-size: 48px;
                        font-weight: 700;
                        color: #FFFFFF;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .footer-message {
                        font-size: 16px;
                        font-weight: 500;
                        color: #00C781;
                        text-align: center;
                        margin-top: 30px;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        font-size: 12px;
                        color: #737373;
                        line-height: 1.6;
                    }
                    .divider {
                        height: 1px;
                        background: rgba(255, 255, 255, 0.1);
                        margin: 30px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="logo-container">
                        <img src="https://raw.githubusercontent.com/ag508/Chain/claude/chain-messaging-platform-design-011CUmDBfoupxn6nTooKuVSV/Chain_App_Icon.png"
                             alt="Chain Logo" class="logo">
                    </div>

                    <h1 class="brand-name">Chain</h1>

                    <div class="content">
                        <div class="welcome-text">Welcome to Chain!</div>

                        <p class="message">
                            Thank you for choosing Chain for secure, private messaging.
                            To complete your registration, please use the verification code below:
                        </p>

                        <div class="otp-container">
                            <div class="otp-label">Your Verification Code</div>
                            <div class="otp-code">$otp</div>
                        </div>

                        <div class="divider"></div>

                        <p class="message">
                            This code will expire in 10 minutes. If you didn't request this code,
                            please ignore this email.
                        </p>

                        <div class="footer-message" style="display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <span>Happy Secure Chatting!</span>
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 2C9.243 2 7 4.243 7 7v3H6c-1.103 0-2 .897-2 2v8c0 1.103.897 2 2 2h12c1.103 0 2-.897 2-2v-8c0-1.103-.897-2-2-2h-1V7c0-2.757-2.243-5-5-5zm6 10v8H6v-8h12zM9 10V7c0-1.654 1.346-3 3-3s3 1.346 3 3v3H9z" fill="#00C781"/>
                            </svg>
                        </div>
                    </div>

                    <div class="footer">
                        <p>This is an automated message from Chain Secure Messaging.</p>
                        <p>End-to-end encrypted • Decentralized • Private</p>
                        <p>&copy; 2025 Chain. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
