import 'package:flutter_test/flutter_test.dart';
import 'package:trust_wallet_flutter/trust_wallet_flutter.dart';

void main() {
  group('TWallet Tests', () {
    late TWallet wallet;
    
    setUp(() {
      wallet = TWallet();
    });
    
    tearDown(() {
      wallet.dispose();
    });
    
    group('Mnemonic Operations', () {
      test('should create valid BIP-39 mnemonic with default strength', () {
        final mnemonic = wallet.createMnemonic();
        expect(mnemonic, isNotEmpty);
        expect(mnemonic.split(' ').length, equals(12)); // 128 bits = 12 words
      });
      
      test('should create valid BIP-39 mnemonic with different strengths', () {
        final testCases = [
          {'strength': 128, 'expectedWords': 12},
          {'strength': 160, 'expectedWords': 15},
          {'strength': 192, 'expectedWords': 18},
          {'strength': 224, 'expectedWords': 21},
          {'strength': 256, 'expectedWords': 24},
        ];
        
        for (final testCase in testCases) {
          final mnemonic = wallet.createMnemonic(strength: testCase['strength'] as int);
          expect(mnemonic.split(' ').length, equals(testCase['expectedWords']));
        }
      });
      
      test('should throw error for invalid mnemonic strength', () {
        expect(() => wallet.createMnemonic(strength: 100), throwsArgumentError);
        expect(() => wallet.createMnemonic(strength: 300), throwsArgumentError);
      });
      
      test('should validate correct mnemonic', () {
        final mnemonic = wallet.createMnemonic();
        expect(() => wallet.importMnemonic(mnemonic), returnsNormally);
      });
      
      test('should reject invalid mnemonic', () {
        expect(() => wallet.importMnemonic('invalid mnemonic words'), throwsException);
        expect(() => wallet.importMnemonic(''), throwsException);
        expect(() => wallet.importMnemonic('word1 word2'), throwsException);
      });
    });
    
    group('Private Key Derivation', () {
      test('should derive Ethereum private key from mnemonic', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        
        expect(privateKey, isNotEmpty);
        expect(privateKey.length, equals(64)); // 32 bytes = 64 hex characters
        expect(RegExp(r'^[0-9a-fA-F]+$').hasMatch(privateKey), isTrue);
      });
      
      test('should derive different keys with passphrase', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey1 = wallet.deriveEthereumPrivateKey(mnemonic);
        final privateKey2 = wallet.deriveEthereumPrivateKey(mnemonic, passphrase: 'test');
        
        expect(privateKey1, isNot(equals(privateKey2)));
      });
      
      test('should derive same key for same mnemonic', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey1 = wallet.deriveEthereumPrivateKey(mnemonic);
        final privateKey2 = wallet.deriveEthereumPrivateKey(mnemonic);
        
        expect(privateKey1, equals(privateKey2));
      });
      
      test('should throw error for invalid mnemonic in derivation', () {
        expect(() => wallet.deriveEthereumPrivateKey('invalid mnemonic'), throwsException);
      });
    });
    
    group('Address Generation', () {
      test('should generate valid Ethereum address from private key', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        final address = wallet.getEthereumAddress(privateKey);
        
        expect(address, isNotEmpty);
        expect(address.startsWith('0x'), isTrue);
        expect(address.length, equals(42)); // 0x + 40 hex characters
        expect(RegExp(r'^0x[0-9a-fA-F]{40}$').hasMatch(address), isTrue);
      });
      
      test('should generate same address for same private key', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        final address1 = wallet.getEthereumAddress(privateKey);
        final address2 = wallet.getEthereumAddress(privateKey);
        
        expect(address1, equals(address2));
      });
      
      test('should throw error for invalid private key', () {
        expect(() => wallet.getEthereumAddress('invalid'), throwsException);
        expect(() => wallet.getEthereumAddress(''), throwsException);
        expect(() => wallet.getEthereumAddress('zzzz'), throwsException);
      });
    });
    
    group('Address Validation', () {
      test('should validate correct Ethereum addresses', () {
        final validAddresses = [
          '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
          '0xdAC17F958D2ee523a2206206994597C13D831ec7',
          '0x0000000000000000000000000000000000000000',
          '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
        ];
        
        for (final address in validAddresses) {
          expect(wallet.isValidEthereumAddress(address), isTrue);
        }
      });
      
      test('should reject invalid Ethereum addresses', () {
        final invalidAddresses = [
          '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82', // Too short
          '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82bb', // Too long
          '742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b', // Missing 0x
          '0xGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG', // Invalid hex
          '', // Empty
          '0x', // Only prefix
        ];
        
        for (final address in invalidAddresses) {
          expect(wallet.isValidEthereumAddress(address), isFalse);
        }
      });
    });
    
    group('ERC-20 Transfer Signing', () {
      test('should sign ERC-20 transfer with valid parameters', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        
        final signedTx = wallet.signErc20Transfer(
          privateKeyHex: privateKey,
          toAddress: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
          amount: '1000000', // 1 USDT
          gasPrice: '20000000000', // 20 Gwei
          gasLimit: '60000',
          nonce: '0',
        );
        
        expect(signedTx, isNotEmpty);
        expect(signedTx.startsWith('0x'), isTrue);
      });
      
      test('should throw error for invalid recipient address', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        
        expect(() => wallet.signErc20Transfer(
          privateKeyHex: privateKey,
          toAddress: 'invalid_address',
          amount: '1000000',
          gasPrice: '20000000000',
          gasLimit: '60000',
          nonce: '0',
        ), throwsException);
      });
      
      test('should throw error for invalid contract address', () {
        final mnemonic = wallet.createMnemonic();
        final privateKey = wallet.deriveEthereumPrivateKey(mnemonic);
        
        expect(() => wallet.signErc20Transfer(
          privateKeyHex: privateKey,
          toAddress: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
          amount: '1000000',
          gasPrice: '20000000000',
          gasLimit: '60000',
          nonce: '0',
          contractAddress: 'invalid_contract',
        ), throwsException);
      });
      
      test('should throw error for invalid private key', () {
        expect(() => wallet.signErc20Transfer(
          privateKeyHex: 'invalid_key',
          toAddress: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
          amount: '1000000',
          gasPrice: '20000000000',
          gasLimit: '60000',
          nonce: '0',
        ), throwsException);
      });
    });
  });
  
  group('Utility Functions', () {
    test('should convert USDT to wei correctly', () {
      expect(WalletUtils.usdtToWei(1.0), equals('1000000'));
      expect(WalletUtils.usdtToWei(0.5), equals('500000'));
      expect(WalletUtils.usdtToWei(100.0), equals('100000000'));
    });
    
    test('should convert wei to USDT correctly', () {
      expect(WalletUtils.weiToUsdt('1000000'), equals(1.0));
      expect(WalletUtils.weiToUsdt('500000'), equals(0.5));
      expect(WalletUtils.weiToUsdt('100000000'), equals(100.0));
    });
    
    test('should convert Gwei to wei correctly', () {
      expect(WalletUtils.gweiToWei(1.0), equals('1000000000'));
      expect(WalletUtils.gweiToWei(20.0), equals('20000000000'));
      expect(WalletUtils.gweiToWei(0.5), equals('500000000'));
    });
    
    test('should convert wei to Gwei correctly', () {
      expect(WalletUtils.weiToGwei('1000000000'), equals(1.0));
      expect(WalletUtils.weiToGwei('20000000000'), equals(20.0));
      expect(WalletUtils.weiToGwei('500000000'), equals(0.5));
    });
    
    test('should validate mnemonic length correctly', () {
      expect(WalletUtils.isValidMnemonicLength('word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12'), isTrue);
      expect(WalletUtils.isValidMnemonicLength('word1 word2'), isFalse);
      expect(WalletUtils.isValidMnemonicLength(''), isFalse);
    });
  });
  
  group('Blockchain Utils', () {
    test('should convert Ether to Wei correctly', () {
      expect(BlockchainUtils.etherToWei(1.0), equals('1000000000000000000'));
      expect(BlockchainUtils.etherToWei(0.5), equals('500000000000000000'));
    });
    
    test('should convert Wei to Ether correctly', () {
      expect(BlockchainUtils.weiToEther('1000000000000000000'), equals(1.0));
      expect(BlockchainUtils.weiToEther('500000000000000000'), equals(0.5));
    });
    
    test('should validate hex strings correctly', () {
      expect(BlockchainUtils.isValidHex('0x123abc'), isTrue);
      expect(BlockchainUtils.isValidHex('123abc'), isTrue);
      expect(BlockchainUtils.isValidHex('0x123xyz'), isFalse);
      expect(BlockchainUtils.isValidHex(''), isFalse);
    });
    
    test('should pad hex strings correctly', () {
      expect(BlockchainUtils.padHex('0x123', 6), equals('000123'));
      expect(BlockchainUtils.padHex('abc', 6), equals('000abc'));
    });
    
    test('should remove leading zeros correctly', () {
      expect(BlockchainUtils.removeLeadingZeros('0x000123'), equals('123'));
      expect(BlockchainUtils.removeLeadingZeros('000abc'), equals('abc'));
    });
  });
  
  group('Production Wallet Example', () {
    late ProductionWalletExample productionWallet;
    
    setUp(() {
      productionWallet = ProductionWalletExample();
    });
    
    tearDown(() {
      productionWallet.dispose();
    });
    
    test('should create new wallet successfully', () async {
      final walletData = await productionWallet.createNewWallet();
      
      expect(walletData['mnemonic'], isNotEmpty);
      expect(walletData['privateKey'], isNotEmpty);
      expect(walletData['address'], isNotEmpty);
      expect(walletData['address']!.startsWith('0x'), isTrue);
    });
    
    test('should restore wallet from mnemonic', () async {
      final walletData = await productionWallet.createNewWallet();
      final mnemonic = walletData['mnemonic']!;
      
      final restoredWallet = await productionWallet.restoreWallet(mnemonic);
      
      expect(restoredWallet['mnemonic'], equals(mnemonic));
      expect(restoredWallet['privateKey'], equals(walletData['privateKey']));
      expect(restoredWallet['address'], equals(walletData['address']));
    });
    
    test('should sign token transfer with validation', () async {
      final walletData = await productionWallet.createNewWallet();
      final privateKey = walletData['privateKey']!;
      
      final signedTx = await productionWallet.signTokenTransfer(
        privateKey: privateKey,
        tokenContract: TokenContracts.usdtEthereum,
        recipient: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
        amount: '1000000',
        gasPrice: '20000000000',
        gasLimit: '60000',
        nonce: '0',
      );
      
      expect(signedTx, isNotEmpty);
      expect(signedTx.startsWith('0x'), isTrue);
    });
    
    test('should throw error for invalid token contract', () async {
      final walletData = await productionWallet.createNewWallet();
      final privateKey = walletData['privateKey']!;
      
      expect(() => productionWallet.signTokenTransfer(
        privateKey: privateKey,
        tokenContract: 'invalid_contract',
        recipient: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
        amount: '1000000',
        gasPrice: '20000000000',
        gasLimit: '60000',
        nonce: '0',
      ), throwsException);
    });
    
    test('should throw error for negative amount', () async {
      final walletData = await productionWallet.createNewWallet();
      final privateKey = walletData['privateKey']!;
      
      expect(() => productionWallet.signTokenTransfer(
        privateKey: privateKey,
        tokenContract: TokenContracts.usdtEthereum,
        recipient: '0x742d35Cc6634C0532925a3b8D3A9B4C0A7e6d82b',
        amount: '-1000000',
        gasPrice: '20000000000',
        gasLimit: '60000',
        nonce: '0',
      ), throwsException);
    });
  });
  
  group('Library Info', () {
    test('should return correct library information', () {
      final info = LibraryInfo.getInfo();
      
      expect(info['name'], equals('Trust Wallet Flutter'));
      expect(info['version'], equals('1.0.0'));
      expect(info['description'], contains('Flutter wrapper'));
      expect(info['author'], contains('Engineer'));
    });
  });
  
  group('Error Handling', () {
    test('should create custom exceptions correctly', () {
      final mnemonicError = MnemonicException('Invalid mnemonic');
      final privateKeyError = PrivateKeyException('Invalid private key');
      final signingError = SigningException('Signing failed');
      final validationError = ValidationException('Validation failed');
      
      expect(mnemonicError.code, equals('MNEMONIC_ERROR'));
      expect(privateKeyError.code, equals('PRIVATE_KEY_ERROR'));
      expect(signingError.code, equals('SIGNING_ERROR'));
      expect(validationError.code, equals('VALIDATION_ERROR'));
    });
  });
}