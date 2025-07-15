import 'twallet.dart';

/// Example usage of the TWallet class
/// This demonstrates all the core functionality including:
/// - Creating and importing BIP-39 mnemonics
/// - Deriving Ethereum private keys
/// - Signing ERC-20 USDT transfers
/// - Getting Ethereum addresses
void main() async {
  // Initialize the TWallet instance
  final wallet = TWallet();
  
  print('🔐 Trust Wallet Core Flutter Integration Example');
  print('================================================\n');
  
  try {
    // Example 1: Create a new BIP-39 mnemonic
    print('1. Creating new BIP-39 mnemonic...');
    final mnemonic = wallet.createMnemonic(strength: 128); // 12 words
    print('✅ Generated mnemonic: $mnemonic\n');
    
    // Example 2: Import and validate existing mnemonic
    print('2. Importing and validating mnemonic...');
    final isValid = wallet.importMnemonic(mnemonic);
    print('✅ Mnemonic is valid: $isValid\n');
    
    // Example 3: Derive Ethereum private key at path m/44'/60'/0'/0/0
    print('3. Deriving Ethereum private key...');
    final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
    print('✅ Private key: $privateKey\n');
    
    // Example 4: Get Ethereum address from private key
    print('4. Getting Ethereum address...');
    final address = wallet.getEthereumAddress(privateKey);
    print('✅ Ethereum address: $address\n');
    
    // Example 5: Sign ERC-20 USDT transfer
    print('5. Signing ERC-20 USDT transfer...');
    
    // Transaction parameters
    final recipientAddress = '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b';
    final usdtAmount = '1000000'; // 1 USDT (6 decimals)
    final gasPrice = '20000000000'; // 20 Gwei
    final gasLimit = '60000'; // Standard ERC-20 gas limit
    final nonce = '0'; // Transaction nonce
    
    final signedTx = wallet.signErc20Transfer(
      privateKeyHex: privateKey,
      toAddress: recipientAddress,
      amount: usdtAmount,
      gasPrice: gasPrice,
      gasLimit: gasLimit,
      nonce: nonce,
    );
    
    print('✅ Signed transaction: $signedTx\n');
    
    // Example 6: Validate Ethereum address
    print('6. Validating Ethereum addresses...');
    final validAddress = '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b';
    final invalidAddress = '0x123invalid';
    
    print('✅ Valid address: ${wallet.isValidEthereumAddress(validAddress)}');
    print('✅ Invalid address: ${wallet.isValidEthereumAddress(invalidAddress)}\n');
    
    // Example 7: Working with different mnemonic strengths
    print('7. Creating mnemonics with different strengths...');
    final mnemonic12 = wallet.createMnemonic(strength: 128); // 12 words
    final mnemonic24 = wallet.createMnemonic(strength: 256); // 24 words
    
    print('✅ 12-word mnemonic: ${mnemonic12.split(' ').length} words');
    print('✅ 24-word mnemonic: ${mnemonic24.split(' ').length} words\n');
    
    // Example 8: Error handling demonstration
    print('8. Error handling examples...');
    
    try {
      // This will throw an error due to invalid mnemonic
      wallet.importMnemonic('invalid mnemonic words');
    } catch (e) {
      print('✅ Caught expected error: ${e.toString()}\n');
    }
    
    try {
      // This will throw an error due to invalid address format
      wallet.isValidEthereumAddress('invalid_address');
    } catch (e) {
      print('✅ Caught expected error: ${e.toString()}\n');
    }
    
    print('🎉 All examples completed successfully!');
    
  } catch (e) {
    print('❌ Error: $e');
  } finally {
    // Clean up resources
    wallet.dispose();
  }
}

/// Advanced example for production use cases
class ProductionWalletExample {
  final TWallet _wallet;
  
  ProductionWalletExample() : _wallet = TWallet();
  
  /// Create a new wallet with secure mnemonic generation
  Future<Map<String, String>> createNewWallet({
    int strength = 128,
    String passphrase = '',
  }) async {
    try {
      // Generate secure mnemonic
      final mnemonic = _wallet.createMnemonic(strength: strength);
      
      // Derive private key
      final privateKey = _wallet.deriveEthereumPrivateKey(mnemonic, passphrase: passphrase);
      
      // Get Ethereum address
      final address = _wallet.getEthereumAddress(privateKey);
      
      return {
        'mnemonic': mnemonic,
        'privateKey': privateKey,
        'address': address,
      };
    } catch (e) {
      throw Exception('Failed to create new wallet: $e');
    }
  }
  
  /// Restore wallet from mnemonic
  Future<Map<String, String>> restoreWallet(
    String mnemonic, {
    String passphrase = '',
  }) async {
    try {
      // Validate mnemonic
      if (!_wallet.importMnemonic(mnemonic)) {
        throw Exception('Invalid mnemonic phrase');
      }
      
      // Derive private key
      final privateKey = _wallet.deriveEthereumPrivateKey(mnemonic, passphrase: passphrase);
      
      // Get Ethereum address
      final address = _wallet.getEthereumAddress(privateKey);
      
      return {
        'mnemonic': mnemonic,
        'privateKey': privateKey,
        'address': address,
      };
    } catch (e) {
      throw Exception('Failed to restore wallet: $e');
    }
  }
  
  /// Sign ERC-20 token transfer with validation
  Future<String> signTokenTransfer({
    required String privateKey,
    required String tokenContract,
    required String recipient,
    required String amount,
    required String gasPrice,
    required String gasLimit,
    required String nonce,
  }) async {
    try {
      // Validate inputs
      if (!_wallet.isValidEthereumAddress(tokenContract)) {
        throw ArgumentError('Invalid token contract address');
      }
      
      if (!_wallet.isValidEthereumAddress(recipient)) {
        throw ArgumentError('Invalid recipient address');
      }
      
      // Validate amount is positive
      final amountBigInt = BigInt.parse(amount);
      if (amountBigInt <= BigInt.zero) {
        throw ArgumentError('Amount must be positive');
      }
      
      // Sign transaction
      final signedTx = _wallet.signErc20Transfer(
        privateKeyHex: privateKey,
        toAddress: recipient,
        amount: amount,
        gasPrice: gasPrice,
        gasLimit: gasLimit,
        nonce: nonce,
        contractAddress: tokenContract,
      );
      
      return signedTx;
    } catch (e) {
      throw Exception('Failed to sign token transfer: $e');
    }
  }
  
  /// Batch sign multiple transactions
  Future<List<String>> signMultipleTransfers(
    List<Map<String, String>> transactions,
  ) async {
    final results = <String>[];
    
    for (final tx in transactions) {
      try {
        final signedTx = await signTokenTransfer(
          privateKey: tx['privateKey']!,
          tokenContract: tx['tokenContract']!,
          recipient: tx['recipient']!,
          amount: tx['amount']!,
          gasPrice: tx['gasPrice']!,
          gasLimit: tx['gasLimit']!,
          nonce: tx['nonce']!,
        );
        results.add(signedTx);
      } catch (e) {
        // In production, you might want to handle individual failures differently
        throw Exception('Failed to sign transaction ${transactions.indexOf(tx)}: $e');
      }
    }
    
    return results;
  }
  
  /// Clean up resources
  void dispose() {
    _wallet.dispose();
  }
}

/// Utility functions for common operations
class WalletUtils {
  /// Convert USDT amount to wei (6 decimals)
  static String usdtToWei(double amount) {
    final wei = (amount * 1000000).toInt();
    return wei.toString();
  }
  
  /// Convert wei to USDT amount (6 decimals)
  static double weiToUsdt(String wei) {
    final amount = BigInt.parse(wei);
    return amount.toDouble() / 1000000;
  }
  
  /// Format gas price in Gwei
  static String gweiToWei(double gwei) {
    final wei = (gwei * 1000000000).toInt();
    return wei.toString();
  }
  
  /// Convert wei to Gwei
  static double weiToGwei(String wei) {
    final amount = BigInt.parse(wei);
    return amount.toDouble() / 1000000000;
  }
  
  /// Generate secure nonce (in production, get from blockchain)
  static String generateNonce() {
    // In production, you should get the nonce from the blockchain
    // This is just for demonstration
    return DateTime.now().millisecondsSinceEpoch.toString();
  }
  
  /// Validate mnemonic word count
  static bool isValidMnemonicLength(String mnemonic) {
    final words = mnemonic.trim().split(' ');
    return [12, 15, 18, 21, 24].contains(words.length);
  }
}