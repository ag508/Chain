package com.chain.app.presentation.contacts

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Utility class for generating QR codes.
 */
object QRCodeGenerator {

    /**
     * Generate a QR code bitmap from the given data.
     *
     * @param data The data to encode in the QR code
     * @param size The size of the QR code (width = height)
     * @return Bitmap of the QR code, or null if generation fails
     */
    fun generateQRCode(data: String, size: Int = 512): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate QR code data for a user profile.
     * Format: chain://user?id=USER_ID&phone=PHONE&name=NAME&key=PUBLIC_KEY
     *
     * @param userId The user's ID
     * @param phoneNumber The user's phone number
     * @param displayName The user's display name
     * @param publicKey The user's public key (optional)
     * @return QR code data string
     */
    fun generateUserQRData(
        userId: String,
        phoneNumber: String,
        displayName: String,
        publicKey: String? = null
    ): String {
        val params = buildList {
            add("id=$userId")
            add("phone=${phoneNumber.replace("+", "%2B")}")
            add("name=${displayName.replace(" ", "%20")}")
            if (publicKey != null) {
                add("key=$publicKey")
            }
        }.joinToString("&")

        return "chain://user?$params"
    }

    /**
     * Parse user data from QR code.
     * Expects format: chain://user?id=USER_ID&phone=PHONE&name=NAME&key=PUBLIC_KEY
     *
     * @param qrData The QR code data string
     * @return Map of parsed parameters, or null if invalid format
     */
    fun parseUserQRData(qrData: String): Map<String, String>? {
        return try {
            if (!qrData.startsWith("chain://user?")) {
                return null
            }

            val params = qrData.substringAfter("chain://user?")
            params.split("&")
                .associate {
                    val (key, value) = it.split("=")
                    key to value.replace("%2B", "+").replace("%20", " ")
                }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
