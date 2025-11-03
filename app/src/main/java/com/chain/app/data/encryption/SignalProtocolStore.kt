package com.chain.app.data.encryption

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.signal.libsignal.protocol.*
import org.signal.libsignal.protocol.state.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Signal Protocol's storage interface.
 * Stores identity keys, pre-keys, and session data securely using Android Keystore.
 */
@Singleton
class SignalProtocolStore @Inject constructor(
    @ApplicationContext private val context: Context
) : org.signal.libsignal.protocol.state.SignalProtocolStore {

    private val gson = Gson()
    private val sharedPrefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "signal_protocol_store",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // IdentityKeyStore implementation
    private var _identityKeyPair: IdentityKeyPair? = null

    override fun getIdentityKeyPair(): IdentityKeyPair {
        if (_identityKeyPair == null) {
            val serialized = sharedPrefs.getString("identity_key_pair", null)
            _identityKeyPair = if (serialized != null) {
                deserializeIdentityKeyPair(serialized)
            } else {
                val newKeyPair = generateIdentityKeyPair()
                saveIdentityKeyPair(newKeyPair)
                newKeyPair
            }
        }
        return _identityKeyPair!!
    }

    override fun getLocalRegistrationId(): Int {
        var registrationId = sharedPrefs.getInt("registration_id", 0)
        if (registrationId == 0) {
            registrationId = generateRegistrationId()
            sharedPrefs.edit().putInt("registration_id", registrationId).apply()
        }
        return registrationId
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        val key = "identity_${address.name}_${address.deviceId}"
        val serialized = android.util.Base64.encodeToString(identityKey.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
        return true
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        val key = "identity_${address.name}_${address.deviceId}"
        val stored = sharedPrefs.getString(key, null) ?: return true
        val storedIdentityKey = IdentityKey(
            android.util.Base64.decode(stored, android.util.Base64.NO_WRAP),
            0
        )
        return storedIdentityKey == identityKey
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        val key = "identity_${address.name}_${address.deviceId}"
        val stored = sharedPrefs.getString(key, null) ?: return null
        return IdentityKey(android.util.Base64.decode(stored, android.util.Base64.NO_WRAP), 0)
    }

    // PreKeyStore implementation
    override fun loadPreKey(preKeyId: Int): PreKeyRecord {
        val key = "prekey_$preKeyId"
        val serialized = sharedPrefs.getString(key, null)
            ?: throw InvalidKeyIdException("No pre-key found for ID: $preKeyId")
        return PreKeyRecord(android.util.Base64.decode(serialized, android.util.Base64.NO_WRAP))
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        val key = "prekey_$preKeyId"
        val serialized = android.util.Base64.encodeToString(record.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        return sharedPrefs.contains("prekey_$preKeyId")
    }

    override fun removePreKey(preKeyId: Int) {
        sharedPrefs.edit().remove("prekey_$preKeyId").apply()
    }

    // SignedPreKeyStore implementation
    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        val key = "signed_prekey_$signedPreKeyId"
        val serialized = sharedPrefs.getString(key, null)
            ?: throw InvalidKeyIdException("No signed pre-key found for ID: $signedPreKeyId")
        return SignedPreKeyRecord(android.util.Base64.decode(serialized, android.util.Base64.NO_WRAP))
    }

    override fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> {
        val records = mutableListOf<SignedPreKeyRecord>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("signed_prekey_") && value is String) {
                records.add(SignedPreKeyRecord(android.util.Base64.decode(value, android.util.Base64.NO_WRAP)))
            }
        }
        return records
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        val key = "signed_prekey_$signedPreKeyId"
        val serialized = android.util.Base64.encodeToString(record.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return sharedPrefs.contains("signed_prekey_$signedPreKeyId")
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        sharedPrefs.edit().remove("signed_prekey_$signedPreKeyId").apply()
    }

    // SessionStore implementation
    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        val key = "session_${address.name}_${address.deviceId}"
        val serialized = sharedPrefs.getString(key, null)
        return if (serialized != null) {
            SessionRecord(android.util.Base64.decode(serialized, android.util.Base64.NO_WRAP))
        } else {
            SessionRecord()
        }
    }

    override fun loadExistingSessions(addresses: MutableList<SignalProtocolAddress>): MutableList<SessionRecord> {
        return addresses.map { loadSession(it) }.toMutableList()
    }

    override fun getSubDeviceSessions(name: String): MutableList<Int> {
        val deviceIds = mutableListOf<Int>()
        sharedPrefs.all.forEach { (key, _) ->
            if (key.startsWith("session_$name")) {
                val deviceId = key.substringAfterLast("_").toIntOrNull()
                if (deviceId != null) {
                    deviceIds.add(deviceId)
                }
            }
        }
        return deviceIds
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        val key = "session_${address.name}_${address.deviceId}"
        val serialized = android.util.Base64.encodeToString(record.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        return sharedPrefs.contains("session_${address.name}_${address.deviceId}")
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        sharedPrefs.edit().remove("session_${address.name}_${address.deviceId}").apply()
    }

    override fun deleteAllSessions(name: String) {
        val keysToRemove = sharedPrefs.all.keys.filter { it.startsWith("session_$name") }
        val editor = sharedPrefs.edit()
        keysToRemove.forEach { editor.remove(it) }
        editor.apply()
    }

    // SenderKeyStore implementation
    override fun storeSenderKey(sender: SignalProtocolAddress, distributionId: java.util.UUID, record: org.signal.libsignal.protocol.groups.state.SenderKeyRecord) {
        val key = "senderkey_${sender.name}_${sender.deviceId}_$distributionId"
        val serialized = android.util.Base64.encodeToString(record.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
    }

    override fun loadSenderKey(sender: SignalProtocolAddress, distributionId: java.util.UUID): org.signal.libsignal.protocol.groups.state.SenderKeyRecord? {
        val key = "senderkey_${sender.name}_${sender.deviceId}_$distributionId"
        val serialized = sharedPrefs.getString(key, null) ?: return null
        return org.signal.libsignal.protocol.groups.state.SenderKeyRecord(
            android.util.Base64.decode(serialized, android.util.Base64.NO_WRAP)
        )
    }

    // KyberPreKeyStore implementation (post-quantum cryptography support)
    override fun loadKyberPreKey(kyberPreKeyId: Int): org.signal.libsignal.protocol.state.KyberPreKeyRecord {
        val key = "kyber_prekey_$kyberPreKeyId"
        val serialized = sharedPrefs.getString(key, null)
            ?: throw InvalidKeyIdException("No Kyber pre-key found for ID: $kyberPreKeyId")
        return org.signal.libsignal.protocol.state.KyberPreKeyRecord(
            android.util.Base64.decode(serialized, android.util.Base64.NO_WRAP)
        )
    }

    override fun loadKyberPreKeys(): MutableList<org.signal.libsignal.protocol.state.KyberPreKeyRecord> {
        val records = mutableListOf<org.signal.libsignal.protocol.state.KyberPreKeyRecord>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("kyber_prekey_") && value is String) {
                records.add(
                    org.signal.libsignal.protocol.state.KyberPreKeyRecord(
                        android.util.Base64.decode(value, android.util.Base64.NO_WRAP)
                    )
                )
            }
        }
        return records
    }

    override fun storeKyberPreKey(kyberPreKeyId: Int, record: org.signal.libsignal.protocol.state.KyberPreKeyRecord) {
        val key = "kyber_prekey_$kyberPreKeyId"
        val serialized = android.util.Base64.encodeToString(record.serialize(), android.util.Base64.NO_WRAP)
        sharedPrefs.edit().putString(key, serialized).apply()
    }

    override fun containsKyberPreKey(kyberPreKeyId: Int): Boolean {
        return sharedPrefs.contains("kyber_prekey_$kyberPreKeyId")
    }

    override fun markKyberPreKeyUsed(kyberPreKeyId: Int) {
        // Mark the key as used - could set a flag or timestamp
        val key = "kyber_prekey_used_$kyberPreKeyId"
        sharedPrefs.edit().putBoolean(key, true).apply()
    }

    // Helper methods
    private fun generateIdentityKeyPair(): IdentityKeyPair {
        val keyPair = org.signal.libsignal.protocol.ecc.Curve.generateKeyPair()
        return IdentityKeyPair(IdentityKey(keyPair.publicKey), keyPair.privateKey)
    }

    private fun generateRegistrationId(): Int {
        return ((Math.random() * 16380) + 1).toInt()
    }

    private fun saveIdentityKeyPair(keyPair: IdentityKeyPair) {
        val serialized = serializeIdentityKeyPair(keyPair)
        sharedPrefs.edit().putString("identity_key_pair", serialized).apply()
    }

    private fun serializeIdentityKeyPair(keyPair: IdentityKeyPair): String {
        val publicKey = android.util.Base64.encodeToString(keyPair.publicKey.serialize(), android.util.Base64.NO_WRAP)
        val privateKey = android.util.Base64.encodeToString(keyPair.privateKey.serialize(), android.util.Base64.NO_WRAP)
        return gson.toJson(mapOf("public" to publicKey, "private" to privateKey))
    }

    private fun deserializeIdentityKeyPair(serialized: String): IdentityKeyPair {
        val map = gson.fromJson(serialized, Map::class.java) as Map<String, String>
        val publicKey = IdentityKey(android.util.Base64.decode(map["public"], android.util.Base64.NO_WRAP), 0)
        val privateKey = org.signal.libsignal.protocol.ecc.Curve.decodePrivatePoint(
            android.util.Base64.decode(map["private"], android.util.Base64.NO_WRAP)
        )
        return IdentityKeyPair(publicKey, privateKey)
    }
}
