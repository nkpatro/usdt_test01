package com.cryptowallet.app.core.crypto

import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.params.MainNetParams
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import java.security.SecureRandom
import java.nio.charset.Charset
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

/**
 * Handles wallet generation, mnemonic creation, and key derivation for all supported cryptocurrencies
 */
class WalletGenerator {
    
    init {
        // Add BouncyCastle provider for cryptographic operations
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    /**
     * Generates a new 12-word BIP-39 mnemonic phrase
     */
    fun generateMnemonic(): List<String> {
        val entropy = ByteArray(16) // 128 bits for 12 words
        SecureRandom().nextBytes(entropy)
        return MnemonicCode.INSTANCE.toMnemonic(entropy)
    }
    
    /**
     * Validates a mnemonic phrase
     */
    fun validateMnemonic(mnemonic: List<String>): Boolean {
        return try {
            MnemonicCode.INSTANCE.check(mnemonic)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generates seed from mnemonic using PBKDF2
     */
    fun generateSeed(mnemonic: List<String>, passphrase: String = ""): ByteArray {
        val mnemonicString = mnemonic.joinToString(" ")
        val salt = "mnemonic$passphrase".toByteArray(Charset.forName("UTF-8"))
        
        val spec = PBEKeySpec(mnemonicString.toCharArray(), salt, 2048, 512)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(spec).encoded
    }
    
    /**
     * Derives a master key from seed
     */
    fun deriveMasterKey(seed: ByteArray): DeterministicKey {
        return HDKeyDerivation.createMasterPrivateKey(seed)
    }
    
    /**
     * Derives a private key for a specific coin type and address index
     */
    fun derivePrivateKey(
        masterKey: DeterministicKey,
        coinType: CoinType,
        addressIndex: Int = 0
    ): DeterministicKey {
        val path = parseBip44Path(coinType.derivationPath, addressIndex)
        var key = masterKey
        
        for (childNumber in path) {
            key = HDKeyDerivation.deriveChildKey(key, childNumber)
        }
        
        return key
    }
    
    /**
     * Generates a wallet address for the given private key and coin type
     */
    fun generateAddress(privateKey: DeterministicKey, coinType: CoinType): String {
        return when (coinType.networkType) {
            NetworkType.BITCOIN -> generateBitcoinAddress(privateKey)
            NetworkType.ETHEREUM -> generateEthereumAddress(privateKey)
            NetworkType.SOLANA -> generateSolanaAddress(privateKey)
            NetworkType.LITECOIN,
            NetworkType.DOGECOIN,
            NetworkType.LUCKYCOIN,
            NetworkType.DIGIBYTES,
            NetworkType.BELLSCOIN,
            NetworkType.JUNKCOIN,
            NetworkType.DINGOCOIN,
            NetworkType.SHIBACOIN,
            NetworkType.CATCOIN,
            NetworkType.BONKSCOIN -> generateScryptBasedAddress(privateKey, coinType)
            NetworkType.PEPECORE -> generatePepecoreAddress(privateKey)
        }
    }
    
    private fun generateBitcoinAddress(privateKey: DeterministicKey): String {
        val params = MainNetParams.get()
        return privateKey.toAddress(params).toString()
    }
    
    private fun generateEthereumAddress(privateKey: DeterministicKey): String {
        val ecKeyPair = ECKeyPair.create(privateKey.privKeyBytes)
        return Keys.getAddress(ecKeyPair)
    }
    
    private fun generateSolanaAddress(privateKey: DeterministicKey): String {
        // Solana uses Ed25519 keys, this is a simplified implementation
        // In production, you'd use the proper Solana SDK
        val keyBytes = privateKey.privKeyBytes
        return org.bitcoinj.core.Base58.encode(keyBytes.sliceArray(0..31))
    }
    
    private fun generateScryptBasedAddress(privateKey: DeterministicKey, coinType: CoinType): String {
        // For Scrypt-based coins, we use similar address generation to Bitcoin
        // but with different network parameters and prefixes
        val params = MainNetParams.get()
        val address = privateKey.toAddress(params).toString()
        
        // Apply coin-specific prefixes if needed
        return coinType.addressPrefix?.let { prefix ->
            prefix + address.substring(1)
        } ?: address
    }
    
    private fun generatePepecoreAddress(privateKey: DeterministicKey): String {
        // Pepecore might use Ethereum-style addresses or its own format
        // This is a placeholder implementation
        return generateEthereumAddress(privateKey)
    }
    
    /**
     * Parses BIP-44 derivation path string into ChildNumber array
     */
    private fun parseBip44Path(path: String, addressIndex: Int): List<ChildNumber> {
        val segments = path.split("/").drop(1) // Remove "m"
        val childNumbers = mutableListOf<ChildNumber>()
        
        for (segment in segments) {
            val isHardened = segment.endsWith("'")
            val number = if (isHardened) {
                segment.dropLast(1).toInt()
            } else {
                segment.toInt()
            }
            
            childNumbers.add(
                if (isHardened) {
                    ChildNumber(number, true)
                } else {
                    ChildNumber(number, false)
                }
            )
        }
        
        // Add address index as the final child number
        childNumbers.add(ChildNumber(addressIndex, false))
        
        return childNumbers
    }
    
    /**
     * Creates a complete wallet for a specific coin type
     */
    data class WalletInfo(
        val coinType: CoinType,
        val address: String,
        val privateKey: String,
        val publicKey: String
    )
    
    fun createWallet(
        mnemonic: List<String>,
        coinType: CoinType,
        passphrase: String = "",
        addressIndex: Int = 0
    ): WalletInfo {
        val seed = generateSeed(mnemonic, passphrase)
        val masterKey = deriveMasterKey(seed)
        val privateKey = derivePrivateKey(masterKey, coinType, addressIndex)
        val address = generateAddress(privateKey, coinType)
        
        return WalletInfo(
            coinType = coinType,
            address = address,
            privateKey = privateKey.privateKeyAsHex,
            publicKey = privateKey.publicKeyAsHex
        )
    }
}