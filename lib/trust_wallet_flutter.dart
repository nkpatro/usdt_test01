/// Trust Wallet Core Flutter Integration Library
/// 
/// This library provides a comprehensive Dart wrapper for Trust Wallet Core
/// with support for BIP-39 mnemonic operations, HD wallet key derivation,
/// and Ethereum ERC-20 token transaction signing.
/// 
/// Features:
/// - BIP-39 mnemonic creation and validation
/// - HD wallet key derivation at path m/44'/60'/0'/0/0
/// - Ethereum address generation
/// - ERC-20 token transfer signing (USDT and other tokens)
/// - Production-ready error handling and memory management
/// - Cross-platform support (Android, iOS, Desktop)
library trust_wallet_flutter;

// Core wallet functionality
export 'twallet.dart';

// FFI bindings for Trust Wallet Core
export 'trust_wallet_core_ffi.dart';

// Example usage and utilities
export 'example_usage.dart';

/// Version information
const String version = '1.0.0';

/// Supported coin types
class CoinType {
  static const int bitcoin = 0;
  static const int ethereum = 60;
  static const int binanceSmartChain = 56;
  static const int polygon = 137;
  static const int avalanche = 43114;
  static const int fantom = 250;
  static const int arbitrum = 42161;
  static const int optimism = 10;
}

/// Common ERC-20 token contract addresses
class TokenContracts {
  // Ethereum Mainnet
  static const String usdtEthereum = '0xdAC17F958D2ee523a2206206994597C13D831ec7';
  static const String usdcEthereum = '0xA0b86a33E6441E7A28c4aE5e73a5e8a9c2b9f2e1';
  static const String daiEthereum = '0x6B175474E89094C44Da98b954EedeAC495271d0F';
  static const String linkEthereum = '0x514910771AF9Ca656af840dff83E8264EcF986CA';
  
  // Binance Smart Chain
  static const String usdtBsc = '0x55d398326f99059fF775485246999027B3197955';
  static const String usdcBsc = '0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d';
  static const String busdBsc = '0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56';
  
  // Polygon
  static const String usdtPolygon = '0xc2132D05D31c914a87C6611C10748AEb04B58e8F';
  static const String usdcPolygon = '0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174';
  static const String daiPolygon = '0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063';
}

/// Gas limit constants for different operations
class GasLimits {
  static const String erc20Transfer = '60000';
  static const String erc20Approve = '80000';
  static const String ethTransfer = '21000';
  static const String contractInteraction = '200000';
}

/// Common gas prices in Gwei
class GasPrices {
  static const String slow = '10';      // 10 Gwei
  static const String standard = '20';  // 20 Gwei
  static const String fast = '30';      // 30 Gwei
  static const String rapid = '50';     // 50 Gwei
}

/// Utility functions for common blockchain operations
class BlockchainUtils {
  /// Convert Ether to Wei
  static String etherToWei(double ether) {
    final wei = (ether * 1000000000000000000).toInt();
    return wei.toString();
  }
  
  /// Convert Wei to Ether
  static double weiToEther(String wei) {
    final amount = BigInt.parse(wei);
    return amount.toDouble() / 1000000000000000000;
  }
  
  /// Convert Gwei to Wei
  static String gweiToWei(double gwei) {
    final wei = (gwei * 1000000000).toInt();
    return wei.toString();
  }
  
  /// Convert Wei to Gwei
  static double weiToGwei(String wei) {
    final amount = BigInt.parse(wei);
    return amount.toDouble() / 1000000000;
  }
  
  /// Get current timestamp in seconds
  static int getCurrentTimestamp() {
    return DateTime.now().millisecondsSinceEpoch ~/ 1000;
  }
  
  /// Validate hex string
  static bool isValidHex(String hex) {
    if (hex.isEmpty) return false;
    final cleanHex = hex.replaceAll('0x', '');
    return RegExp(r'^[0-9a-fA-F]+$').hasMatch(cleanHex);
  }
  
  /// Pad hex string to specified length
  static String padHex(String hex, int length) {
    final cleanHex = hex.replaceAll('0x', '');
    return cleanHex.padLeft(length, '0');
  }
  
  /// Remove leading zeros from hex string
  static String removeLeadingZeros(String hex) {
    final cleanHex = hex.replaceAll('0x', '');
    return cleanHex.replaceAll(RegExp(r'^0+'), '');
  }
}

/// Error types for better error handling
class TWalletException implements Exception {
  final String message;
  final String code;
  
  const TWalletException(this.message, this.code);
  
  @override
  String toString() => 'TWalletException($code): $message';
}

class MnemonicException extends TWalletException {
  const MnemonicException(String message) : super(message, 'MNEMONIC_ERROR');
}

class PrivateKeyException extends TWalletException {
  const PrivateKeyException(String message) : super(message, 'PRIVATE_KEY_ERROR');
}

class SigningException extends TWalletException {
  const SigningException(String message) : super(message, 'SIGNING_ERROR');
}

class ValidationException extends TWalletException {
  const ValidationException(String message) : super(message, 'VALIDATION_ERROR');
}

/// Library information and metadata
class LibraryInfo {
  static const String name = 'Trust Wallet Flutter';
  static const String version = '1.0.0';
  static const String description = 'Flutter wrapper for Trust Wallet Core';
  static const String author = 'Senior Mobile-Blockchain Engineer';
  static const String repository = 'https://github.com/example/trust-wallet-flutter';
  
  /// Get library information as a map
  static Map<String, String> getInfo() {
    return {
      'name': name,
      'version': version,
      'description': description,
      'author': author,
      'repository': repository,
    };
  }
  
  /// Print library information
  static void printInfo() {
    final info = getInfo();
    print('=== $name ===');
    info.forEach((key, value) {
      print('$key: $value');
    });
  }
}