# Trust Wallet Flutter

A comprehensive Flutter wrapper for Trust Wallet Core with support for BIP-39 mnemonic operations, HD wallet key derivation, and Ethereum ERC-20 token transaction signing.

## Features

- ✅ **BIP-39 Mnemonic Support**: Create and validate mnemonic phrases with customizable entropy strength
- ✅ **HD Wallet Key Derivation**: Derive Ethereum private keys at path `m/44'/60'/0'/0/0`
- ✅ **Ethereum Address Generation**: Generate Ethereum addresses from private keys
- ✅ **ERC-20 Token Signing**: Sign USDT and other ERC-20 token transfers
- ✅ **Production-Ready**: Comprehensive error handling and memory management
- ✅ **Cross-Platform**: Support for Android, iOS, and Desktop platforms
- ✅ **Type-Safe**: Full Dart type safety with comprehensive documentation

## Installation

### Prerequisites

1. **Trust Wallet Core Library**: Download the appropriate native library for your platform:
   - Android: `libTrustWalletCore.so`
   - iOS: `TrustWalletCore.framework`
   - Linux: `libTrustWalletCore.so`
   - macOS: `libTrustWalletCore.dylib`
   - Windows: `TrustWalletCore.dll`

2. **Add to pubspec.yaml**:
```yaml
dependencies:
  flutter:
    sdk: flutter
  ffi: ^2.0.2
  path: ^1.8.3
  hex: ^0.2.0
  crypto: ^3.0.3
  convert: ^3.1.1
```

### Platform-Specific Setup

#### Android
1. Place `libTrustWalletCore.so` in `android/app/src/main/jniLibs/arm64-v8a/`
2. Add to `android/app/build.gradle`:
```gradle
android {
    packagingOptions {
        pickFirst '**/libTrustWalletCore.so'
    }
}
```

#### iOS
1. Add `TrustWalletCore.framework` to your iOS project
2. Ensure it's linked in Build Phases → Link Binary With Libraries

#### Desktop
1. Place the appropriate library file in the same directory as your executable
2. For Linux/macOS, you may need to set `LD_LIBRARY_PATH` or `DYLD_LIBRARY_PATH`

## Basic Usage

### Initialize the Wallet

```dart
import 'package:trust_wallet_flutter/trust_wallet_flutter.dart';

void main() {
  final wallet = TWallet();
  
  // Your code here
  
  // Don't forget to dispose when done
  wallet.dispose();
}
```

### Create a New Wallet

```dart
// Create a new BIP-39 mnemonic (12 words by default)
final mnemonic = wallet.createMnemonic();
print('Generated mnemonic: $mnemonic');

// Create with different strength (24 words)
final mnemonic24 = wallet.createMnemonic(strength: 256);
print('24-word mnemonic: $mnemonic24');

// Derive Ethereum private key
final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
print('Private key: $privateKey');

// Get Ethereum address
final address = wallet.getEthereumAddress(privateKey);
print('Address: $address');
```

### Import Existing Wallet

```dart
const existingMnemonic = 'abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about';

// Validate and import mnemonic
final isValid = wallet.importMnemonic(existingMnemonic);
print('Mnemonic is valid: $isValid');

// Derive keys from imported mnemonic
final privateKey = wallet.deriveEthereumPrivateKey(existingMnemonic);
final address = wallet.getEthereumAddress(privateKey);
```

### Sign ERC-20 Token Transfer

```dart
// Sign USDT transfer
final signedTx = wallet.signErc20Transfer(
  privateKeyHex: privateKey,
  toAddress: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
  amount: '1000000', // 1 USDT (6 decimals)
  gasPrice: '20000000000', // 20 Gwei
  gasLimit: '60000',
  nonce: '0',
  contractAddress: TokenContracts.usdtEthereum, // Optional, defaults to USDT
);

print('Signed transaction: $signedTx');
```

## Production Usage

### Advanced Wallet Management

```dart
import 'package:trust_wallet_flutter/trust_wallet_flutter.dart';

class MyWalletService {
  final _productionWallet = ProductionWalletExample();
  
  Future<Map<String, String>> createWallet() async {
    return await _productionWallet.createNewWallet(
      strength: 256, // 24 words for extra security
      passphrase: 'optional-passphrase',
    );
  }
  
  Future<String> sendUSDT({
    required String privateKey,
    required String recipient,
    required double amount,
    required double gasPriceGwei,
    required String nonce,
  }) async {
    return await _productionWallet.signTokenTransfer(
      privateKey: privateKey,
      tokenContract: TokenContracts.usdtEthereum,
      recipient: recipient,
      amount: WalletUtils.usdtToWei(amount),
      gasPrice: WalletUtils.gweiToWei(gasPriceGwei),
      gasLimit: GasLimits.erc20Transfer,
      nonce: nonce,
    );
  }
  
  void dispose() {
    _productionWallet.dispose();
  }
}
```

### Batch Operations

```dart
// Sign multiple transactions
final transactions = [
  {
    'privateKey': privateKey1,
    'tokenContract': TokenContracts.usdtEthereum,
    'recipient': '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
    'amount': '1000000',
    'gasPrice': '20000000000',
    'gasLimit': '60000',
    'nonce': '0',
  },
  // Add more transactions...
];

final signedTxs = await productionWallet.signMultipleTransfers(transactions);
```

## Utility Functions

### Token Amount Conversions

```dart
// Convert USDT to wei (6 decimals)
final usdtWei = WalletUtils.usdtToWei(1.5); // "1500000"

// Convert wei to USDT
final usdtAmount = WalletUtils.weiToUsdt('1500000'); // 1.5

// Convert Gwei to wei
final weiAmount = WalletUtils.gweiToWei(20.0); // "20000000000"
```

### Blockchain Utilities

```dart
// Convert Ether to Wei
final ethWei = BlockchainUtils.etherToWei(0.1); // "100000000000000000"

// Validate hex strings
final isValid = BlockchainUtils.isValidHex('0x123abc'); // true

// Pad hex strings
final padded = BlockchainUtils.padHex('0x123', 8); // "00000123"
```

## Supported Networks and Tokens

### Networks
- Ethereum Mainnet (Chain ID: 1)
- Binance Smart Chain (Chain ID: 56)
- Polygon (Chain ID: 137)
- Avalanche (Chain ID: 43114)
- Arbitrum (Chain ID: 42161)
- Optimism (Chain ID: 10)

### Common Token Contracts
```dart
// Ethereum
TokenContracts.usdtEthereum  // USDT
TokenContracts.usdcEthereum  // USDC
TokenContracts.daiEthereum   // DAI

// Binance Smart Chain
TokenContracts.usdtBsc       // USDT
TokenContracts.usdcBsc       // USDC
TokenContracts.busdBsc       // BUSD

// Polygon
TokenContracts.usdtPolygon   // USDT
TokenContracts.usdcPolygon   // USDC
TokenContracts.daiPolygon    // DAI
```

## Error Handling

The library provides comprehensive error handling with custom exception types:

```dart
try {
  final mnemonic = wallet.createMnemonic();
  final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
  final signedTx = wallet.signErc20Transfer(/* parameters */);
} on MnemonicException catch (e) {
  print('Mnemonic error: ${e.message}');
} on PrivateKeyException catch (e) {
  print('Private key error: ${e.message}');
} on SigningException catch (e) {
  print('Signing error: ${e.message}');
} on ValidationException catch (e) {
  print('Validation error: ${e.message}');
} catch (e) {
  print('General error: $e');
}
```

## Testing

Run the comprehensive test suite:

```bash
flutter test
```

The test suite covers:
- Mnemonic creation and validation
- Private key derivation
- Address generation
- Transaction signing
- Error handling
- Utility functions

## Security Considerations

1. **Private Key Storage**: Never store private keys in plaintext. Use secure storage solutions like:
   - Android: Android Keystore
   - iOS: iOS Keychain
   - Desktop: Secure storage libraries

2. **Mnemonic Backup**: Encourage users to securely backup their mnemonic phrases offline

3. **Network Security**: Always use HTTPS when communicating with blockchain nodes

4. **Validation**: Always validate addresses and amounts before signing transactions

5. **Gas Estimation**: Implement proper gas estimation to avoid failed transactions

## Platform Notes

### Android
- Minimum SDK version: 21
- Target SDK version: 33+
- Ensure proper ProGuard rules if using code obfuscation

### iOS
- Minimum deployment target: iOS 12.0
- Ensure proper code signing for the Trust Wallet Core framework

### Desktop
- Tested on Windows 10+, macOS 10.14+, Ubuntu 18.04+
- May require additional system dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Changelog

### Version 1.0.0
- Initial release
- BIP-39 mnemonic support
- HD wallet key derivation
- Ethereum address generation
- ERC-20 token signing
- Cross-platform support
- Comprehensive test suite

## Support

For issues and questions:
1. Check the [GitHub Issues](https://github.com/example/trust-wallet-flutter/issues)
2. Review the [Trust Wallet Core Documentation](https://developer.trustwallet.com/wallet-core)
3. Submit a new issue with detailed reproduction steps

## Acknowledgments

- [Trust Wallet Core](https://github.com/trustwallet/wallet-core) - The underlying cryptographic library
- [Flutter](https://flutter.dev/) - The UI toolkit
- [Dart FFI](https://dart.dev/guides/libraries/c-interop) - For native library integration