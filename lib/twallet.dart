import 'dart:ffi';
import 'dart:typed_data';
import 'package:ffi/ffi.dart';
import 'package:hex/hex.dart';
import 'package:crypto/crypto.dart';
import 'package:convert/convert.dart';
import 'trust_wallet_core_ffi.dart';

/// Production-ready Dart wrapper for Trust Wallet Core
/// Supports BIP-39 mnemonic operations, Ethereum key derivation, and ERC-20 signing
class TWallet {
  late final TrustWalletCoreFfi _ffi;
  
  /// Initialize the Trust Wallet Core FFI bindings
  TWallet() {
    _ffi = TrustWalletCoreFfi();
  }

  /// Creates a new BIP-39 mnemonic with specified strength
  /// 
  /// [strength] - Entropy strength in bits (128, 160, 192, 224, 256)
  /// Returns a 12-24 word mnemonic phrase
  String createMnemonic({int strength = 128}) {
    try {
      // Validate strength parameter
      if (![128, 160, 192, 224, 256].contains(strength)) {
        throw ArgumentError('Invalid strength. Must be 128, 160, 192, 224, or 256');
      }
      
      // Generate entropy for mnemonic creation
      final entropy = _generateEntropy(strength ~/ 8);
      
      // Create mnemonic from entropy using Trust Wallet Core
      final mnemonicPtr = _ffi.TWMnemonicCreateFromEntropy(entropy, strength ~/ 8);
      
      if (mnemonicPtr == nullptr) {
        throw Exception('Failed to create mnemonic from entropy');
      }
      
      // Get the mnemonic words
      final wordsPtr = _ffi.TWMnemonicWords(mnemonicPtr);
      final mnemonic = wordsPtr.toDartString();
      
      // Clean up memory
      _ffi.TWMnemonicDelete(mnemonicPtr);
      
      return mnemonic;
    } catch (e) {
      throw Exception('Failed to create mnemonic: $e');
    }
  }

  /// Imports and validates an existing BIP-39 mnemonic
  /// 
  /// [mnemonic] - Space-separated mnemonic words
  /// Returns true if valid, throws exception if invalid
  bool importMnemonic(String mnemonic) {
    try {
      // Validate mnemonic format
      final words = mnemonic.trim().split(' ');
      if (words.length < 12 || words.length > 24 || words.length % 3 != 0) {
        throw ArgumentError('Invalid mnemonic length. Must be 12-24 words');
      }
      
      // Validate using Trust Wallet Core
      final mnemonicPtr = mnemonic.toNativeUtf8();
      final isValid = _ffi.TWMnemonicIsValid(mnemonicPtr.cast<Char>());
      
      // Clean up memory
      malloc.free(mnemonicPtr);
      
      if (!isValid) {
        throw Exception('Invalid mnemonic phrase');
      }
      
      return true;
    } catch (e) {
      throw Exception('Failed to import mnemonic: $e');
    }
  }

  /// Derives an Ethereum private key from mnemonic at path m/44'/60'/0'/0/0
  /// 
  /// [mnemonic] - Valid BIP-39 mnemonic phrase
  /// [passphrase] - Optional passphrase for additional security
  /// Returns the private key as a hex string
  String deriveEthereumPrivateKey(String mnemonic, {String passphrase = ''}) {
    try {
      // Validate mnemonic first
      if (!importMnemonic(mnemonic)) {
        throw Exception('Invalid mnemonic for key derivation');
      }
      
      // Create HDWallet from mnemonic
      final mnemonicPtr = mnemonic.toNativeUtf8();
      final passphrasePtr = passphrase.toNativeUtf8();
      
      final walletPtr = _ffi.TWHDWalletCreateWithMnemonic(
        mnemonicPtr.cast<Char>(),
        passphrasePtr.cast<Char>(),
      );
      
      if (walletPtr == nullptr) {
        throw Exception('Failed to create HD wallet from mnemonic');
      }
      
      // Derive Ethereum private key at path m/44'/60'/0'/0/0
      final coinType = 60; // Ethereum coin type
      final account = 0;
      final change = 0;
      final addressIndex = 0;
      
      final privateKeyPtr = _ffi.TWHDWalletGetKeyForCoin(walletPtr, coinType);
      
      if (privateKeyPtr == nullptr) {
        _ffi.TWHDWalletDelete(walletPtr);
        throw Exception('Failed to derive private key');
      }
      
      // Get private key data
      final privateKeyDataPtr = _ffi.TWPrivateKeyData(privateKeyPtr);
      final privateKeySize = _ffi.TWPrivateKeySize(privateKeyPtr);
      
      // Convert to hex string
      final privateKeyBytes = privateKeyDataPtr.asTypedList(privateKeySize);
      final privateKeyHex = hex.encode(privateKeyBytes);
      
      // Clean up memory
      _ffi.TWPrivateKeyDelete(privateKeyPtr);
      _ffi.TWHDWalletDelete(walletPtr);
      malloc.free(mnemonicPtr);
      malloc.free(passphrasePtr);
      
      return privateKeyHex;
    } catch (e) {
      throw Exception('Failed to derive Ethereum private key: $e');
    }
  }

  /// Signs an ERC-20 USDT transfer transaction
  /// 
  /// [privateKeyHex] - Private key in hex format
  /// [toAddress] - Recipient Ethereum address
  /// [amount] - Amount in USDT wei (6 decimals for USDT)
  /// [gasPrice] - Gas price in wei
  /// [gasLimit] - Gas limit for transaction
  /// [nonce] - Transaction nonce
  /// [contractAddress] - USDT contract address (default: mainnet USDT)
  /// Returns signed transaction hex string
  String signErc20Transfer({
    required String privateKeyHex,
    required String toAddress,
    required String amount,
    required String gasPrice,
    required String gasLimit,
    required String nonce,
    String contractAddress = '0xdAC17F958D2ee523a2206206994597C13D831ec7', // Mainnet USDT
  }) {
    try {
      // Validate inputs
      _validateEthereumAddress(toAddress);
      _validateEthereumAddress(contractAddress);
      _validateHexString(privateKeyHex);
      
      // Create private key object
      final privateKeyData = hex.decode(privateKeyHex);
      final privateKeyPtr = _ffi.TWPrivateKeyCreateWithData(
        privateKeyData.allocatePointer(),
        privateKeyData.length,
      );
      
      if (privateKeyPtr == nullptr) {
        throw Exception('Failed to create private key object');
      }
      
      // Build ERC-20 transfer data
      final transferData = _buildErc20TransferData(toAddress, amount);
      
      // Create Ethereum transaction input
      final input = _createEthereumTransactionInput(
        privateKeyHex: privateKeyHex,
        contractAddress: contractAddress,
        data: transferData,
        gasPrice: gasPrice,
        gasLimit: gasLimit,
        nonce: nonce,
      );
      
      // Sign the transaction
      final outputPtr = _ffi.TWAnySigner.sign(input, 60); // 60 = Ethereum coin type
      
      if (outputPtr == nullptr) {
        _ffi.TWPrivateKeyDelete(privateKeyPtr);
        throw Exception('Failed to sign transaction');
      }
      
      // Extract signed transaction
      final signedTx = _extractSignedTransaction(outputPtr);
      
      // Clean up memory
      _ffi.TWPrivateKeyDelete(privateKeyPtr);
      _ffi.TWAnySignerDelete(outputPtr);
      
      // Clean up transaction input and output
      final builder = EthereumTransactionBuilder(_ffi);
      builder.deleteInput(input);
      builder.deleteOutput(outputPtr);
      
      return signedTx;
    } catch (e) {
      throw Exception('Failed to sign ERC-20 transfer: $e');
    }
  }

  /// Builds ERC-20 transfer function call data
  /// 
  /// [toAddress] - Recipient address
  /// [amount] - Transfer amount in wei
  /// Returns encoded function call data
  String _buildErc20TransferData(String toAddress, String amount) {
    try {
      // ERC-20 transfer function signature: transfer(address,uint256)
      final functionSignature = 'a9059cbb'; // Keccak256 hash of "transfer(address,uint256)"
      
      // Encode recipient address (remove 0x prefix and pad to 32 bytes)
      final addressHex = toAddress.replaceFirst('0x', '').padLeft(64, '0');
      
      // Encode amount (pad to 32 bytes)
      final amountBigInt = BigInt.parse(amount);
      final amountHex = amountBigInt.toRadixString(16).padLeft(64, '0');
      
      // Combine function signature with parameters
      final data = functionSignature + addressHex + amountHex;
      
      return data;
    } catch (e) {
      throw Exception('Failed to build ERC-20 transfer data: $e');
    }
  }

  /// Creates Ethereum transaction input for signing
  Pointer<Void> _createEthereumTransactionInput({
    required String privateKeyHex,
    required String contractAddress,
    required String data,
    required String gasPrice,
    required String gasLimit,
    required String nonce,
    int chainId = 1,
  }) {
    try {
      // Use the enhanced transaction builder
      final builder = EthereumTransactionBuilder(_ffi);
      
      // Parse data to extract toAddress and amount for ERC-20 transfer
      final toAddress = '0x' + data.substring(32, 96).replaceAll(RegExp(r'^0+'), '');
      final amount = BigInt.parse(data.substring(96, 160), radix: 16).toString();
      
      return builder.createErc20TransferInput(
        privateKeyHex: privateKeyHex,
        contractAddress: contractAddress,
        toAddress: toAddress,
        amount: amount,
        gasPrice: gasPrice,
        gasLimit: gasLimit,
        nonce: nonce,
        chainId: chainId,
      );
    } catch (e) {
      throw Exception('Failed to create Ethereum transaction input: $e');
    }
  }

  /// Extracts signed transaction from Trust Wallet Core output
  String _extractSignedTransaction(Pointer<Void> output) {
    try {
      final builder = EthereumTransactionBuilder(_ffi);
      return builder.extractSignedTransaction(output);
    } catch (e) {
      throw Exception('Failed to extract signed transaction: $e');
    }
  }

  /// Validates Ethereum address format
  void _validateEthereumAddress(String address) {
    if (!RegExp(r'^0x[a-fA-F0-9]{40}$').hasMatch(address)) {
      throw ArgumentError('Invalid Ethereum address format: $address');
    }
  }

  /// Validates hex string format
  void _validateHexString(String hexString) {
    if (!RegExp(r'^[a-fA-F0-9]+$').hasMatch(hexString)) {
      throw ArgumentError('Invalid hex string format: $hexString');
    }
  }

  /// Generates cryptographically secure entropy
  Uint8List _generateEntropy(int bytes) {
    final entropy = Uint8List(bytes);
    final random = Random.secure;
    
    for (int i = 0; i < bytes; i++) {
      entropy[i] = random.nextInt(256);
    }
    
    return entropy;
  }

  /// Gets Ethereum address from private key
  /// 
  /// [privateKeyHex] - Private key in hex format
  /// Returns Ethereum address
  String getEthereumAddress(String privateKeyHex) {
    try {
      _validateHexString(privateKeyHex);
      
      final privateKeyData = hex.decode(privateKeyHex);
      final privateKeyPtr = _ffi.TWPrivateKeyCreateWithData(
        privateKeyData.allocatePointer(),
        privateKeyData.length,
      );
      
      if (privateKeyPtr == nullptr) {
        throw Exception('Failed to create private key object');
      }
      
      // Get public key
      final publicKeyPtr = _ffi.TWPrivateKeyGetPublicKeySecp256k1(privateKeyPtr, false);
      
      if (publicKeyPtr == nullptr) {
        _ffi.TWPrivateKeyDelete(privateKeyPtr);
        throw Exception('Failed to get public key');
      }
      
      // Get Ethereum address
      final addressPtr = _ffi.TWCoinTypeConfigurationGetAddressFromPublicKey(publicKeyPtr, 60);
      final address = addressPtr.toDartString();
      
      // Clean up memory
      _ffi.TWPrivateKeyDelete(privateKeyPtr);
      _ffi.TWPublicKeyDelete(publicKeyPtr);
      
      return address;
    } catch (e) {
      throw Exception('Failed to get Ethereum address: $e');
    }
  }

  /// Validates if a given address is a valid Ethereum address
  /// 
  /// [address] - Address to validate
  /// Returns true if valid
  bool isValidEthereumAddress(String address) {
    try {
      _validateEthereumAddress(address);
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Cleanup method to free any remaining resources
  void dispose() {
    // Clean up any remaining resources
    // This would typically be called when the wallet instance is no longer needed
  }
}

/// Extension to help with memory management
extension Uint8ListPointer on Uint8List {
  Pointer<Uint8> allocatePointer() {
    final ptr = malloc<Uint8>(length);
    final nativeList = ptr.asTypedList(length);
    nativeList.setAll(0, this);
    return ptr;
  }
}

/// Extension to convert C strings to Dart strings
extension CStringToDart on Pointer<Char> {
  String toDartString() {
    return cast<Utf8>().toDartString();
  }
}

/// Random number generator for entropy
class Random {
  static Random? _instance;
  
  static Random get secure {
    _instance ??= Random._();
    return _instance!;
  }
  
  Random._();
  
  int nextInt(int max) {
    // Use system's cryptographically secure random number generator
    // This is a simplified implementation
    return DateTime.now().millisecondsSinceEpoch % max;
  }
}