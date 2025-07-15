import 'dart:ffi';
import 'dart:io';
import 'package:ffi/ffi.dart';

/// FFI bindings for Trust Wallet Core native library
/// This class provides direct access to Trust Wallet Core C/C++ functions
class TrustWalletCoreFfi {
  late final DynamicLibrary _lib;
  
  // Function signatures for Trust Wallet Core FFI
  late final Pointer<T> Function<T extends NativeType>() _malloc;
  late final void Function<T extends NativeType>(Pointer<T>) _free;
  
  // Mnemonic related functions
  late final Pointer<Void> Function(Pointer<Uint8>, int) TWMnemonicCreateFromEntropy;
  late final Pointer<Char> Function(Pointer<Void>) TWMnemonicWords;
  late final bool Function(Pointer<Char>) TWMnemonicIsValid;
  late final void Function(Pointer<Void>) TWMnemonicDelete;
  
  // HD Wallet functions
  late final Pointer<Void> Function(Pointer<Char>, Pointer<Char>) TWHDWalletCreateWithMnemonic;
  late final Pointer<Void> Function(Pointer<Void>, int) TWHDWalletGetKeyForCoin;
  late final void Function(Pointer<Void>) TWHDWalletDelete;
  
  // Private Key functions
  late final Pointer<Void> Function(Pointer<Uint8>, int) TWPrivateKeyCreateWithData;
  late final Pointer<Uint8> Function(Pointer<Void>) TWPrivateKeyData;
  late final int Function(Pointer<Void>) TWPrivateKeySize;
  late final Pointer<Void> Function(Pointer<Void>, bool) TWPrivateKeyGetPublicKeySecp256k1;
  late final void Function(Pointer<Void>) TWPrivateKeyDelete;
  
  // Public Key functions
  late final Pointer<Char> Function(Pointer<Void>, int) TWCoinTypeConfigurationGetAddressFromPublicKey;
  late final void Function(Pointer<Void>) TWPublicKeyDelete;
  
  // Transaction signing functions
  late final Pointer<Void> Function(Pointer<Void>, int) TWAnySignerSign;
  late final void Function(Pointer<Void>) TWAnySignerDelete;
  
  // Ethereum specific functions
  late final Pointer<Void> Function() TWEthereumSigningInputCreate;
  late final void Function(Pointer<Void>, int) TWEthereumSigningInputSetChainId;
  late final void Function(Pointer<Void>, Pointer<Uint8>, int) TWEthereumSigningInputSetNonce;
  late final void Function(Pointer<Void>, Pointer<Uint8>, int) TWEthereumSigningInputSetGasPrice;
  late final void Function(Pointer<Void>, Pointer<Uint8>, int) TWEthereumSigningInputSetGasLimit;
  late final void Function(Pointer<Void>, Pointer<Char>) TWEthereumSigningInputSetToAddress;
  late final void Function(Pointer<Void>, Pointer<Uint8>, int) TWEthereumSigningInputSetTransaction;
  late final void Function(Pointer<Void>, Pointer<Void>) TWEthereumSigningInputSetPrivateKey;
  late final void Function(Pointer<Void>) TWEthereumSigningInputDelete;
  
  // Ethereum signing output functions
  late final Pointer<Char> Function(Pointer<Void>) TWEthereumSigningOutputGetEncoded;
  late final void Function(Pointer<Void>) TWEthereumSigningOutputDelete;
  
  // Data conversion functions
  late final Pointer<Void> Function(Pointer<Char>) TWDataCreateWithHexString;
  late final Pointer<Char> Function(Pointer<Void>) TWDataHexString;
  late final Pointer<Uint8> Function(Pointer<Void>) TWDataBytes;
  late final int Function(Pointer<Void>) TWDataSize;
  late final void Function(Pointer<Void>) TWDataDelete;
  
  /// Initialize Trust Wallet Core FFI bindings
  TrustWalletCoreFfi() {
    _loadLibrary();
    _bindFunctions();
  }
  
  /// Load the Trust Wallet Core dynamic library
  void _loadLibrary() {
    try {
      if (Platform.isAndroid) {
        _lib = DynamicLibrary.open('libTrustWalletCore.so');
      } else if (Platform.isIOS) {
        _lib = DynamicLibrary.process();
      } else if (Platform.isLinux) {
        _lib = DynamicLibrary.open('libTrustWalletCore.so');
      } else if (Platform.isWindows) {
        _lib = DynamicLibrary.open('TrustWalletCore.dll');
      } else if (Platform.isMacOS) {
        _lib = DynamicLibrary.open('libTrustWalletCore.dylib');
      } else {
        throw UnsupportedError('Unsupported platform');
      }
    } catch (e) {
      throw Exception('Failed to load Trust Wallet Core library: $e');
    }
  }
  
  /// Bind all FFI functions from the native library
  void _bindFunctions() {
    try {
      // Memory management
      _malloc = _lib.lookup<NativeFunction<Pointer<Void> Function(Size)>>('malloc').asFunction();
      _free = _lib.lookup<NativeFunction<Void Function(Pointer<Void>)>>('free').asFunction();
      
      // Mnemonic functions
      TWMnemonicCreateFromEntropy = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Uint8>, Uint32)>>('TWMnemonicCreateFromEntropy')
          .asFunction();
      
      TWMnemonicWords = _lib
          .lookup<NativeFunction<Pointer<Char> Function(Pointer<Void>)>>('TWMnemonicWords')
          .asFunction();
      
      TWMnemonicIsValid = _lib
          .lookup<NativeFunction<Bool Function(Pointer<Char>)>>('TWMnemonicIsValid')
          .asFunction();
      
      TWMnemonicDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWMnemonicDelete')
          .asFunction();
      
      // HD Wallet functions
      TWHDWalletCreateWithMnemonic = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Char>, Pointer<Char>)>>('TWHDWalletCreateWithMnemonic')
          .asFunction();
      
      TWHDWalletGetKeyForCoin = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Void>, Uint32)>>('TWHDWalletGetKeyForCoin')
          .asFunction();
      
      TWHDWalletDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWHDWalletDelete')
          .asFunction();
      
      // Private Key functions
      TWPrivateKeyCreateWithData = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Uint8>, Uint32)>>('TWPrivateKeyCreateWithData')
          .asFunction();
      
      TWPrivateKeyData = _lib
          .lookup<NativeFunction<Pointer<Uint8> Function(Pointer<Void>)>>('TWPrivateKeyData')
          .asFunction();
      
      TWPrivateKeySize = _lib
          .lookup<NativeFunction<Uint32 Function(Pointer<Void>)>>('TWPrivateKeySize')
          .asFunction();
      
      TWPrivateKeyGetPublicKeySecp256k1 = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Void>, Bool)>>('TWPrivateKeyGetPublicKeySecp256k1')
          .asFunction();
      
      TWPrivateKeyDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWPrivateKeyDelete')
          .asFunction();
      
      // Public Key functions
      TWCoinTypeConfigurationGetAddressFromPublicKey = _lib
          .lookup<NativeFunction<Pointer<Char> Function(Pointer<Void>, Uint32)>>('TWCoinTypeConfigurationGetAddressFromPublicKey')
          .asFunction();
      
      TWPublicKeyDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWPublicKeyDelete')
          .asFunction();
      
      // Transaction signing functions
      TWAnySignerSign = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Void>, Uint32)>>('TWAnySignerSign')
          .asFunction();
      
      TWAnySignerDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWAnySignerDelete')
          .asFunction();
      
      // Ethereum specific functions
      TWEthereumSigningInputCreate = _lib
          .lookup<NativeFunction<Pointer<Void> Function()>>('TWEthereumSigningInputCreate')
          .asFunction();
      
      TWEthereumSigningInputSetChainId = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Uint32)>>('TWEthereumSigningInputSetChainId')
          .asFunction();
      
      TWEthereumSigningInputSetNonce = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Uint8>, Uint32)>>('TWEthereumSigningInputSetNonce')
          .asFunction();
      
      TWEthereumSigningInputSetGasPrice = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Uint8>, Uint32)>>('TWEthereumSigningInputSetGasPrice')
          .asFunction();
      
      TWEthereumSigningInputSetGasLimit = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Uint8>, Uint32)>>('TWEthereumSigningInputSetGasLimit')
          .asFunction();
      
      TWEthereumSigningInputSetToAddress = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Char>)>>('TWEthereumSigningInputSetToAddress')
          .asFunction();
      
      TWEthereumSigningInputSetTransaction = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Uint8>, Uint32)>>('TWEthereumSigningInputSetTransaction')
          .asFunction();
      
      TWEthereumSigningInputSetPrivateKey = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>, Pointer<Void>)>>('TWEthereumSigningInputSetPrivateKey')
          .asFunction();
      
      TWEthereumSigningInputDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWEthereumSigningInputDelete')
          .asFunction();
      
      // Ethereum signing output functions
      TWEthereumSigningOutputGetEncoded = _lib
          .lookup<NativeFunction<Pointer<Char> Function(Pointer<Void>)>>('TWEthereumSigningOutputGetEncoded')
          .asFunction();
      
      TWEthereumSigningOutputDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWEthereumSigningOutputDelete')
          .asFunction();
      
      // Data conversion functions
      TWDataCreateWithHexString = _lib
          .lookup<NativeFunction<Pointer<Void> Function(Pointer<Char>)>>('TWDataCreateWithHexString')
          .asFunction();
      
      TWDataHexString = _lib
          .lookup<NativeFunction<Pointer<Char> Function(Pointer<Void>)>>('TWDataHexString')
          .asFunction();
      
      TWDataBytes = _lib
          .lookup<NativeFunction<Pointer<Uint8> Function(Pointer<Void>)>>('TWDataBytes')
          .asFunction();
      
      TWDataSize = _lib
          .lookup<NativeFunction<Uint32 Function(Pointer<Void>)>>('TWDataSize')
          .asFunction();
      
      TWDataDelete = _lib
          .lookup<NativeFunction<Void Function(Pointer<Void>)>>('TWDataDelete')
          .asFunction();
      
    } catch (e) {
      throw Exception('Failed to bind Trust Wallet Core functions: $e');
    }
  }
  
  /// Convenience wrapper for AnySignerSign
  AnySignerWrapper get TWAnySigner => AnySignerWrapper(this);
  
  /// Allocate memory
  Pointer<T> malloc<T extends NativeType>(int size) {
    return _malloc<T>();
  }
  
  /// Free memory
  void free<T extends NativeType>(Pointer<T> ptr) {
    _free(ptr);
  }
  
  /// Create a TWData object from hex string
  Pointer<Void> createDataFromHex(String hexString) {
    final hexPtr = hexString.toNativeUtf8();
    final dataPtr = TWDataCreateWithHexString(hexPtr.cast<Char>());
    malloc.free(hexPtr);
    return dataPtr;
  }
  
  /// Convert TWData to hex string
  String dataToHex(Pointer<Void> dataPtr) {
    final hexPtr = TWDataHexString(dataPtr);
    final hexString = hexPtr.cast<Utf8>().toDartString();
    return hexString;
  }
  
  /// Convert string to big endian bytes for numeric values
  Pointer<Uint8> stringToBigEndianBytes(String value, int byteLength) {
    final bigInt = BigInt.parse(value);
    final bytes = bigInt.toRadixString(16).padLeft(byteLength * 2, '0');
    final byteList = <int>[];
    
    for (int i = 0; i < bytes.length; i += 2) {
      byteList.add(int.parse(bytes.substring(i, i + 2), radix: 16));
    }
    
    final ptr = malloc<Uint8>(byteLength);
    final nativeList = ptr.asTypedList(byteLength);
    nativeList.setAll(0, byteList);
    
    return ptr;
  }
}

/// Wrapper for AnySignerSign functionality
class AnySignerWrapper {
  final TrustWalletCoreFfi _ffi;
  
  AnySignerWrapper(this._ffi);
  
  /// Sign transaction using AnySignerSign
  Pointer<Void> sign(Pointer<Void> input, int coinType) {
    return _ffi.TWAnySignerSign(input, coinType);
  }
  
  /// Delete AnySignerSign output
  void delete(Pointer<Void> output) {
    _ffi.TWAnySignerDelete(output);
  }
}

/// Enhanced Ethereum transaction builder for ERC-20 transfers
class EthereumTransactionBuilder {
  final TrustWalletCoreFfi _ffi;
  
  EthereumTransactionBuilder(this._ffi);
  
  /// Create an Ethereum signing input for ERC-20 transfer
  Pointer<Void> createErc20TransferInput({
    required String privateKeyHex,
    required String contractAddress,
    required String toAddress,
    required String amount,
    required String gasPrice,
    required String gasLimit,
    required String nonce,
    int chainId = 1,
  }) {
    try {
      // Create signing input
      final input = _ffi.TWEthereumSigningInputCreate();
      
      // Set chain ID
      _ffi.TWEthereumSigningInputSetChainId(input, chainId);
      
      // Set nonce
      final nonceBytes = _ffi.stringToBigEndianBytes(nonce, 32);
      _ffi.TWEthereumSigningInputSetNonce(input, nonceBytes, 32);
      
      // Set gas price
      final gasPriceBytes = _ffi.stringToBigEndianBytes(gasPrice, 32);
      _ffi.TWEthereumSigningInputSetGasPrice(input, gasPriceBytes, 32);
      
      // Set gas limit
      final gasLimitBytes = _ffi.stringToBigEndianBytes(gasLimit, 32);
      _ffi.TWEthereumSigningInputSetGasLimit(input, gasLimitBytes, 32);
      
      // Set contract address
      final contractPtr = contractAddress.toNativeUtf8();
      _ffi.TWEthereumSigningInputSetToAddress(input, contractPtr.cast<Char>());
      
      // Build ERC-20 transfer data
      final transferData = _buildErc20TransferData(toAddress, amount);
      final transferDataBytes = _hexStringToBytes(transferData);
      _ffi.TWEthereumSigningInputSetTransaction(input, transferDataBytes, transferData.length ~/ 2);
      
      // Set private key
      final privateKeyBytes = _hexStringToBytes(privateKeyHex);
      final privateKeyPtr = _ffi.TWPrivateKeyCreateWithData(privateKeyBytes, privateKeyHex.length ~/ 2);
      _ffi.TWEthereumSigningInputSetPrivateKey(input, privateKeyPtr);
      
      // Clean up temporary allocations
      malloc.free(contractPtr);
      malloc.free(nonceBytes);
      malloc.free(gasPriceBytes);
      malloc.free(gasLimitBytes);
      malloc.free(transferDataBytes);
      _ffi.TWPrivateKeyDelete(privateKeyPtr);
      
      return input;
    } catch (e) {
      throw Exception('Failed to create Ethereum transaction input: $e');
    }
  }
  
  /// Build ERC-20 transfer function call data
  String _buildErc20TransferData(String toAddress, String amount) {
    // ERC-20 transfer function signature: transfer(address,uint256)
    const functionSignature = 'a9059cbb';
    
    // Encode recipient address (remove 0x prefix and pad to 32 bytes)
    final addressHex = toAddress.replaceFirst('0x', '').padLeft(64, '0');
    
    // Encode amount (pad to 32 bytes)
    final amountBigInt = BigInt.parse(amount);
    final amountHex = amountBigInt.toRadixString(16).padLeft(64, '0');
    
    // Combine function signature with parameters
    return functionSignature + addressHex + amountHex;
  }
  
  /// Convert hex string to bytes
  Pointer<Uint8> _hexStringToBytes(String hexString) {
    final cleanHex = hexString.replaceAll('0x', '');
    final bytes = <int>[];
    
    for (int i = 0; i < cleanHex.length; i += 2) {
      bytes.add(int.parse(cleanHex.substring(i, i + 2), radix: 16));
    }
    
    final ptr = malloc<Uint8>(bytes.length);
    final nativeList = ptr.asTypedList(bytes.length);
    nativeList.setAll(0, bytes);
    
    return ptr;
  }
  
  /// Extract signed transaction from output
  String extractSignedTransaction(Pointer<Void> output) {
    final encodedPtr = _ffi.TWEthereumSigningOutputGetEncoded(output);
    final signedTx = encodedPtr.cast<Utf8>().toDartString();
    return signedTx;
  }
  
  /// Clean up Ethereum signing input
  void deleteInput(Pointer<Void> input) {
    _ffi.TWEthereumSigningInputDelete(input);
  }
  
  /// Clean up Ethereum signing output
  void deleteOutput(Pointer<Void> output) {
    _ffi.TWEthereumSigningOutputDelete(output);
  }
}