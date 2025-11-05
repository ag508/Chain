package com.chain.app.data.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Key management service for P2P message signing and verification.
 * Uses Android Keystore for hardware-backed key storage and EncryptedSharedPreferences.
 */
@Singleton
class KeyManagementService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAlias = "chain_signing_key"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    // Encrypted shared preferences for storing public key
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "chain_crypto_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Generate or retrieve existing key pair for signing.
     */
    fun getOrCreateKeyPair(): Result<KeyPair> {
        return try {
            // Check if key already exists
            if (keyStore.containsAlias(keyAlias)) {
                val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
                val publicKeyString = prefs.getString("public_key", null)

                if (publicKeyString != null) {
                    val publicKey = decodePublicKey(publicKeyString)
                    val keyPair = KeyPair(publicKey, privateKey)
                    Timber.d("Retrieved existing key pair")
                    return Result.success(keyPair)
                }
            }

            // Generate new key pair
            val keyPair = generateKeyPair()
            Timber.d("Generated new key pair")
            Result.success(keyPair)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get or create key pair")
            Result.failure(e)
        }
    }

    /**
     * Generate new key pair using Android Keystore.
     */
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setUserAuthenticationRequired(false) // No biometric for signing
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        val keyPair = keyPairGenerator.generateKeyPair()

        // Store public key
        val publicKeyString = encodePublicKey(keyPair.public)
        prefs.edit().putString("public_key", publicKeyString).apply()

        return keyPair
    }

    /**
     * Get public key as Base64 string.
     */
    fun getPublicKeyString(): String? {
        return try {
            val keyPair = getOrCreateKeyPair().getOrNull()
            keyPair?.let { encodePublicKey(it.public) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get public key string")
            null
        }
    }

    /**
     * Sign data with private key.
     */
    fun sign(data: ByteArray): Result<ByteArray> {
        return try {
            val keyPair = getOrCreateKeyPair().getOrThrow()

            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(keyPair.private)
            signature.update(data)

            val signatureBytes = signature.sign()

            Timber.d("Signed data (${data.size} bytes) -> signature (${signatureBytes.size} bytes)")
            Result.success(signatureBytes)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sign data")
            Result.failure(e)
        }
    }

    /**
     * Verify signature with public key.
     */
    fun verify(data: ByteArray, signatureBytes: ByteArray, publicKeyString: String): Result<Boolean> {
        return try {
            val publicKey = decodePublicKey(publicKeyString)

            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initVerify(publicKey)
            signature.update(data)

            val isValid = signature.verify(signatureBytes)

            Timber.d("Signature verification: $isValid")
            Result.success(isValid)
        } catch (e: Exception) {
            Timber.e(e, "Failed to verify signature")
            Result.failure(e)
        }
    }

    /**
     * Generate peer ID from public key.
     */
    fun generatePeerId(): String {
        val publicKey = getPublicKeyString() ?: "unknown"
        return "peer-${publicKey.hashCode().toString(16)}"
    }

    // ========== Helper Methods ==========

    private fun encodePublicKey(publicKey: PublicKey): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(publicKey.encoded)
        } else {
            android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.DEFAULT)
        }
    }

    private fun decodePublicKey(publicKeyString: String): PublicKey {
        val keyBytes = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(publicKeyString)
        } else {
            android.util.Base64.decode(publicKeyString, android.util.Base64.DEFAULT)
        }

        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * Delete key pair (use with caution).
     */
    fun deleteKeyPair(): Result<Unit> {
        return try {
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
                prefs.edit().remove("public_key").apply()
                Timber.w("Deleted key pair")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete key pair")
            Result.failure(e)
        }
    }
}
