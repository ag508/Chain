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
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <title>Chain Verification Code</title>
              <link href="https://fonts.googleapis.com/css2?family=Zen+Dots&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
              <style>
                /* Email client reset */
                body, table, td, div, p, a {
                  -webkit-text-size-adjust: 100%;
                  -ms-text-size-adjust: 100%;
                }
                table {
                  border-collapse: collapse !important;
                }
                table, td, tr {
                  border: none !important;
                }
                img {
                  border: 0;
                  line-height: 100%;
                  outline: none;
                  text-decoration: none;
                  display: block;
                }
                a {
                  text-decoration: none;
                }
            
                /* Unified content container fix (perfect rounded corners) */
                .content {
                  backdrop-filter: blur(18px);
                  -webkit-backdrop-filter: blur(18px);
                  border-radius: 24px;
                  overflow: hidden;
                  border: none !important;
                  background-clip: padding-box !important;
                  position: relative;
                  isolation: isolate;
                  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
                  transition: background 0.3s ease, box-shadow 0.3s ease;
                }
            
                /* Auto dark/light theme */
                @media (prefers-color-scheme: dark) {
                  body {
                    background: linear-gradient(135deg, #0C0C0C 0%, #1A1A1A 100%) !important;
                    color: #FFFFFF !important;
                  }
                  .content {
                    background: rgba(25, 25, 25, 0.75) !important;
                    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.6) !important;
                  }
                  .brand-name, .main-heading, .otp-code {
                    color: #FFFFFF !important;
                  }
                  .otp-container {
                    background: rgba(255, 255, 255, 0.07) !important;
                  }
                  .otp-label, .subtext, .footer {
                    color: #B3B3B3 !important;
                  }
                  .divider {
                    background: rgba(255, 255, 255, 0.1) !important;
                  }
                  .secure-text {
                    color: #00C781 !important;
                  }
                  .secure-icon {
                    fill: #00C781 !important;
                  }
                  .logo-bg {
                    background: rgba(255, 255, 255, 0.1) !important;
                  }
                }
            
                @media (prefers-color-scheme: light) {
                  body {
                    background: #F5F5F7 !important;
                    color: #111 !important;
                  }
                  .content {
                    background: rgba(255, 255, 255, 0.8) !important;
                    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08) !important;
                    border: 1px solid rgba(255, 255, 255, 0.4) !important;
                  }
                  .brand-name, .main-heading, .otp-code {
                    color: #111 !important;
                  }
                  .otp-container {
                    background: rgba(0, 0, 0, 0.05) !important;
                  }
                  .otp-label {
                    color: #666 !important;
                  }
                  .subtext, .footer {
                    color: #555 !important;
                  }
                  .divider {
                    background: rgba(0, 0, 0, 0.1) !important;
                  }
                  .secure-text {
                    color: #00C781 !important;
                  }
                  .secure-icon {
                    fill: #00C781 !important;
                  }
                  .logo-bg {
                    background: rgba(255, 255, 255, 0.8) !important;
                  }
                }
            
                /* Responsive fix */
                @media only screen and (max-width: 600px) {
                  .container {
                    width: 90% !important;
                    padding: 20px !important;
                  }
                  .brand-name {
                    font-size: 30px !important;
                  }
                  .otp-code {
                    font-size: 32px !important;
                    letter-spacing: 6px !important;
                  }
                }
              </style>
            </head>
            
            <body style="margin:0; padding:0; font-family:'Inter', Arial, sans-serif; background:#F5F5F7; color:#111;">
              <center>
                <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
                  <tr>
                    <td align="center" style="padding: 40px 10px;">
                      <table class="container" width="600" cellpadding="0" cellspacing="0" border="0" role="presentation" style="max-width:600px; background:transparent;">
                        
                        <!-- Logo -->
                        <tr>
                          <td align="center" style="padding-bottom: 20px;">
                            <img src="https://raw.githubusercontent.com/ag508/Chain/claude/chain-messaging-platform-design-011CUmDBfoupxn6nTooKuVSV/Chain_App_Icon.png"
                                 alt="Chain Logo"
                                 width="110"
                                 height="110"
                                 class="logo-bg"
                                 style="border-radius:28px; background:rgba(255,255,255,0.8); padding:12px; box-shadow:0 6px 24px rgba(0,0,0,0.2);">
                          </td>
                        </tr>
            
                        <!-- Brand name -->
                        <tr>
                          <td align="center" class="brand-name" style="font-family:'Zen Dots', cursive; font-size:42px; color:#111; letter-spacing:2px; padding-bottom:25px;">
                            Chain
                          </td>
                        </tr>
            
                        <!-- Main content -->
                        <tr>
                          <td class="content" style="padding:40px 30px;">
                            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
                              
                              <tr>
                                <td align="center" class="main-heading" style="font-size:24px; font-weight:600; padding-bottom:15px;">
                                  Welcome to Chain
                                </td>
                              </tr>
            
                              <tr>
                                <td align="center" class="subtext" style="font-size:16px; line-height:1.6; padding-bottom:25px;">
                                  Thank you for choosing Chain for secure, private messaging.<br>
                                  To complete your registration, please use the verification code below:
                                </td>
                              </tr>
            
                              <!-- OTP box -->
                              <tr>
                                <td align="center" class="otp-container" style="border-radius:16px; padding:28px 20px;">
                                  <div class="otp-label" style="font-size:14px; text-transform:uppercase; letter-spacing:1.2px; margin-bottom:10px;">Your Verification Code</div>
                                  <div class="otp-code" style="font-family:'Courier New', monospace; font-size:44px; font-weight:700; letter-spacing:10px;">$otp</div>
                                </td>
                              </tr>
            
                              <!-- Divider -->
                              <tr>
                                <td style="padding-top:30px; padding-bottom:30px;">
                                  <div class="divider" style="height:1px; margin:0 10px;"></div>
                                </td>
                              </tr>
            
                              <!-- Expiration message -->
                              <tr>
                                <td align="center" class="subtext" style="font-size:16px; line-height:1.6;">
                                  This code will expire in 10 minutes.<br>
                                  If you didn't request this code, please ignore this email.
                                </td>
                              </tr>
            
                              <!-- Footer message -->
                              <tr>
                                <td align="center" style="padding-top:25px;">
                                  <table role="presentation" align="center" style="margin:auto;">
                                    <tr>
                                      <td align="center" valign="middle" style="padding-right:8px;">
                                        <svg class="secure-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="#00C781" viewBox="0 0 24 24">
                                          <path d="M12 22c5.421 0 10-4.579 10-10S17.421 2 12 2 2 6.579 2 12s4.579 10 10 10Zm0-2a8 8 0 1 1 0-16 8 8 0 0 1 0 16ZM9.5 12a2.5 2.5 0 0 1 5 0c0 1.5-2.5 3.5-2.5 3.5S9.5 13.5 9.5 12Zm2.5.5a.5.5 0 1 0 0-1 .5.5 0 0 0 0 1Z"/>
                                        </svg>
                                      </td>
                                      <td align="center" valign="middle" class="secure-text" style="font-size:16px; font-weight:500;">
                                        Secure Chatting with Chain
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
            
                            </table>
                          </td>
                        </tr>
            
                        <!-- Footer -->
                        <tr>
                          <td class="footer" align="center" style="font-size:12px; line-height:1.6; padding-top:35px;">
                            <p style="margin:0;">This is an automated message from Chain Secure Messaging.</p>
                            <p style="margin:4px 0;">End-to-end encrypted • Decentralized • Private</p>
                            <p style="margin:0;">&copy; 2025 Chain. All rights reserved.</p>
                          </td>
                        </tr>
            
                      </table>
                    </td>
                  </tr>
                </table>
              </center>
            </body>
            </html>

        """.trimIndent()
    }
}
