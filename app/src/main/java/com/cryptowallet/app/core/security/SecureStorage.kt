package com.cryptowallet.app.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/**
 * Handles secure storage of sensitive data using Android Keystore and EncryptedSharedPreferences
 */
class SecureStorage(private val context: Context) {
    
    companion object {
        private const val KEYSTORE_ALIAS = "CryptoWalletMasterKey"
        private const val SHARED_PREFS_NAME = "secure_crypto_wallet_prefs"
        private const val PRIVATE_KEYS_PREFIX = "private_key_"
        private const val MNEMONIC_KEY = "wallet_mnemonic"
        private const val PIN_HASH_KEY = "pin_hash"
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
        private const val AUTO_LOCK_DURATION_KEY = "auto_lock_duration"
        private const val MFA_SECRET_KEY = "mfa_secret"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        SHARED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Stores encrypted mnemonic phrase
     */
    suspend fun storeMnemonic(mnemonic: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val mnemonicString = mnemonic.joinToString("|")
            encryptedSharedPreferences.edit()
                .putString(MNEMONIC_KEY, mnemonicString)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves and decrypts mnemonic phrase
     */
    suspend fun getMnemonic(): List<String>? = withContext(Dispatchers.IO) {
        try {
            val mnemonicString = encryptedSharedPreferences.getString(MNEMONIC_KEY, null)
            mnemonicString?.split("|")
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Stores encrypted private key for a specific coin
     */
    suspend fun storePrivateKey(coinSymbol: String, privateKey: String): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences.edit()
                    .putString("$PRIVATE_KEYS_PREFIX$coinSymbol", privateKey)
                    .apply()
                true
            } catch (e: Exception) {
                false
            }
        }
    
    /**
     * Retrieves decrypted private key for a specific coin
     */
    suspend fun getPrivateKey(coinSymbol: String): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString("$PRIVATE_KEYS_PREFIX$coinSymbol", null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Stores hashed PIN for local authentication
     */
    suspend fun storePinHash(pinHash: String): Boolean = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putString(PIN_HASH_KEY, pinHash)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves stored PIN hash
     */
    suspend fun getPinHash(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(PIN_HASH_KEY, null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Stores MFA secret key
     */
    suspend fun storeMfaSecret(secret: String): Boolean = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putString(MFA_SECRET_KEY, secret)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves MFA secret key
     */
    suspend fun getMfaSecret(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(MFA_SECRET_KEY, null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Sets biometric authentication preference
     */
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedSharedPreferences.edit()
            .putBoolean(BIOMETRIC_ENABLED_KEY, enabled)
            .apply()
    }
    
    /**
     * Checks if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)
    }
    
    /**
     * Sets auto-lock duration in minutes
     */
    fun setAutoLockDuration(minutes: Int) {
        encryptedSharedPreferences.edit()
            .putInt(AUTO_LOCK_DURATION_KEY, minutes)
            .apply()
    }
    
    /**
     * Gets auto-lock duration in minutes
     */
    fun getAutoLockDuration(): Int {
        return encryptedSharedPreferences.getInt(AUTO_LOCK_DURATION_KEY, 5) // Default 5 minutes
    }
    
    /**
     * Checks if wallet has been initialized (mnemonic exists)
     */
    suspend fun isWalletInitialized(): Boolean = withContext(Dispatchers.IO) {
        getMnemonic() != null
    }
    
    /**
     * Clears all stored data (for wallet reset)
     */
    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets all stored private keys
     */
    suspend fun getAllPrivateKeys(): Map<String, String> = withContext(Dispatchers.IO) {
        val keys = mutableMapOf<String, String>()
        try {
            val allPrefs = encryptedSharedPreferences.all
            for ((key, value) in allPrefs) {
                if (key.startsWith(PRIVATE_KEYS_PREFIX) && value is String) {
                    val coinSymbol = key.removePrefix(PRIVATE_KEYS_PREFIX)
                    keys[coinSymbol] = value
                }
            }
        } catch (e: Exception) {
            // Return empty map on error
        }
        keys
    }
    
    /**
     * Additional encryption layer for extra sensitive operations
     */
    private fun generateKeyStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    private fun getOrCreateKeyStoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            generateKeyStoreKey()
        }
    }
    
    /**
     * Extra secure encryption for critical operations
     */
    fun encryptWithKeyStore(data: String): String {
        val secretKey = getOrCreateKeyStoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encryptedData = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv
        
        // Combine IV and encrypted data
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    /**
     * Decrypts data encrypted with KeyStore
     */
    fun decryptWithKeyStore(encryptedData: String): String {
        val secretKey = getOrCreateKeyStoreKey()
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // Extract IV and encrypted data
        val iv = combined.sliceArray(0..11) // GCM IV is 12 bytes
        val encrypted = combined.sliceArray(12 until combined.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        val decryptedData = cipher.doFinal(encrypted)
        return String(decryptedData)
    }
}