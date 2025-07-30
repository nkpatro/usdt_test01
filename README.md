# Mobile Cryptocurrency Wallet for Android

A comprehensive mobile cryptocurrency wallet built with Kotlin and Jetpack Compose, supporting Litecoin, Dogecoin, and USDT with advanced security features and real-time price tracking.

## 🚀 Features

### Core Functionality
- **Multi-Cryptocurrency Support**: Litecoin (LTC), Dogecoin (DOGE), and USDT
- **BIP-39 Mnemonic Generation**: Secure seed phrase generation and validation
- **Scrypt Key Derivation**: Enhanced security for Scrypt-based cryptocurrencies
- **Real-time Price Updates**: Live price data from CoinGecko API
- **QR Code Scanner**: CameraX-powered address scanning with validation
- **In-app Swaps**: DEX integration via 0x Protocol with security validations

### Security Features
- **Biometric Authentication**: Fingerprint and face unlock support
- **PIN Protection**: Secondary authentication layer
- **Encrypted Storage**: Android Keystore and EncryptedSharedPreferences
- **Transaction Validation**: Multiple security checks for large transactions
- **Address Validation**: Comprehensive cryptocurrency address pattern matching
- **Slippage Protection**: Smart contract interaction safeguards

### User Interface
- **Modern Design**: Material Design 3 with Jetpack Compose
- **Dark/Light Themes**: System-aware theme switching
- **Responsive Layout**: Optimized for various screen sizes
- **Smooth Animations**: Fluid transitions and micro-interactions
- **Accessibility**: Full accessibility support with screen readers

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Async Processing**: Kotlin Coroutines + Flow
- **Security**: BouncyCastle + BitcoinJ

### Project Structure
```
app/
├── src/main/java/com/mobilewallet/
│   ├── crypto/                 # Cryptographic operations
│   │   └── ScryptKeyDerivation.kt
│   ├── data/
│   │   ├── api/               # API interfaces and models
│   │   │   └── CoinGeckoApi.kt
│   │   ├── model/             # Data models
│   │   │   ├── Cryptocurrency.kt
│   │   │   ├── Transaction.kt
│   │   │   └── UserSettings.kt
│   │   └── repository/        # Data repositories
│   │       └── PriceRepository.kt
│   ├── dex/                   # DEX integration
│   │   └── DexIntegration.kt
│   └── ui/                    # User interface
│       ├── home/              # Home screen
│       │   └── HomeScreen.kt
│       └── qr/                # QR scanner
│           └── QRScannerScreen.kt
└── build.gradle.kts           # Dependencies and build config
```

## 📱 Core Components

### 1. Data Models

#### Cryptocurrency Model
```kotlin
@Entity(tableName = "cryptocurrencies")
data class Cryptocurrency(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val balance: BigDecimal,
    val price: BigDecimal,
    val priceChange24h: Double,
    val logoUrl: String,
    val explorerUrl: String,
    // ... additional fields
)
```

#### Transaction Model
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val hash: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: BigDecimal,
    val fee: BigDecimal,
    val timestamp: Long,
    val type: TransactionType,
    val status: TransactionStatus,
    // ... additional fields
)
```

### 2. Scrypt Key Derivation

The `ScryptKeyDerivation` class provides secure key generation for Scrypt-based cryptocurrencies:

```kotlin
class ScryptKeyDerivation {
    fun generateMnemonic(entropyBits: Int = 128): List<String>
    fun mnemonicToSeed(mnemonic: List<String>, passphrase: String = ""): ByteArray
    fun generateLitecoinAddress(privateKey: ByteArray, addressType: AddressType = AddressType.LEGACY): String
    fun generateDogecoinAddress(privateKey: ByteArray): String
    fun generateWalletFromMnemonic(mnemonic: List<String>): Map<CryptoNetwork, List<WalletAddress>>
}
```

**Supported Networks:**
- Bitcoin (BTC) - Mainnet and Testnet
- Litecoin (LTC) - Mainnet and Testnet  
- Dogecoin (DOGE) - Mainnet and Testnet

**Address Types:**
- Legacy (P2PKH)
- Script (P2SH)
- SegWit (P2WPKH)
- Nested SegWit (P2SH-P2WPKH)

### 3. Real-time Price Fetching

The `PriceRepository` handles real-time cryptocurrency price data:

```kotlin
@Singleton
class PriceRepository {
    fun startPriceUpdates(scope: CoroutineScope)
    suspend fun fetchLatestPrices(): Result<Map<String, PriceUpdate>>
    fun getPriceUpdatesForCrypto(cryptoId: String): Flow<PriceUpdate?>
    suspend fun updateCryptocurrencyPrices(cryptocurrencies: List<Cryptocurrency>): List<Cryptocurrency>
}
```

**Features:**
- 30-second update intervals
- Exponential backoff retry logic
- Error handling and user notifications
- Caching for offline support
- Flow-based reactive updates

### 4. QR Code Scanner

The QR scanner uses CameraX and ML Kit for address recognition:

```kotlin
@Composable
fun QRScannerScreen(
    state: QRScannerState,
    onAction: (QRScannerAction) -> Unit,
    onAddressConfirmed: (String) -> Unit
)
```

**Supported Address Formats:**
- Bitcoin: Legacy, SegWit (Bech32)
- Litecoin: Legacy, SegWit
- Dogecoin: Legacy format
- Ethereum: Standard hex format

**Security Features:**
- Real-time address validation
- Pattern matching for each cryptocurrency
- User confirmation before address use
- Flash control for low-light scanning

### 5. DEX Integration

The DEX service integrates with 0x Protocol for secure token swaps:

```kotlin
@Singleton
class DexIntegrationService {
    suspend fun getSwapQuote(fromToken: String, toToken: String, amount: BigDecimal): Result<SwapTransaction>
    suspend fun executeSwap(swapTransaction: SwapTransaction, userConfirmation: Boolean): Result<String>
    fun monitorSwapStatus(txHash: String): Flow<SwapStatus>
}
```

**Security Considerations:**
- Maximum slippage validation (5% limit)
- Trusted DEX source verification
- Large transaction warnings
- Gas price reasonableness checks
- Multi-step user confirmation
- Transaction expiration handling

## 🔒 Security Architecture

### Cryptographic Security
- **BIP-39 Standard**: Industry-standard mnemonic generation
- **Scrypt Key Stretching**: Additional security for key derivation
- **Hardware Security Module**: Android Keystore integration
- **Secure Random**: Cryptographically secure entropy generation

### Application Security
- **Encrypted Storage**: All sensitive data encrypted at rest
- **Biometric Authentication**: Hardware-backed biometric verification
- **Auto-lock**: Configurable timeout for app locking
- **Screenshot Prevention**: Secure content flags in sensitive screens
- **Root Detection**: Runtime application self-protection

### Network Security
- **Certificate Pinning**: Prevents man-in-the-middle attacks
- **TLS 1.3**: Modern transport layer security
- **Request Signing**: API request authentication
- **Rate Limiting**: Protection against API abuse

## 🎨 User Interface

### Home Screen
The main wallet interface built with Jetpack Compose:

```kotlin
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    // Portfolio summary card
    PortfolioSummaryCard(...)
    
    // Action buttons (Send, Receive, Swap)
    ActionButtonsRow(...)
    
    // Cryptocurrency assets list
    AssetsSection(...)
}
```

**Features:**
- Real-time portfolio valuation
- 24-hour price change indicators
- Quick action buttons
- Pull-to-refresh functionality
- Balance privacy toggle

### Design System
- **Material Design 3**: Latest design guidelines
- **Dynamic Colors**: System theme integration
- **Typography Scale**: Consistent text styling
- **Color Semantics**: Status-aware color usage
- **Spacing System**: Consistent layout patterns

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android SDK 24+
- Git

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/mobile-wallet.git
cd mobile-wallet
```

2. **Open in Android Studio**
```bash
# Open the project in Android Studio
./gradlew build
```

3. **Configure API Keys**
Create `local.properties` and add:
```properties
COINGECKO_API_KEY=your_api_key_here
ZEROX_API_KEY=your_api_key_here
```

4. **Build and Run**
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Dependencies

The app uses these key dependencies:

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Cryptography
implementation("org.bouncycastle:bcprov-jdk15on:1.70")
implementation("org.bitcoinj:bitcoinj-core:0.16.2")

// Camera & ML Kit
implementation("androidx.camera:camera-core:1.3.0")
implementation("com.google.mlkit:barcode-scanning:17.2.0")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.48")
```

## 🔧 Configuration

### Network Configuration
Configure supported networks in `ScryptKeyDerivation.kt`:

```kotlin
enum class CryptoNetwork(
    val coinType: Int,
    val addressPrefix: Int,
    val scriptPrefix: Int,
    val privateKeyPrefix: Int,
    val bech32Prefix: String? = null
) {
    LITECOIN(2, 0x30, 0x32, 0xB0, "ltc"),
    DOGECOIN(3, 0x1E, 0x16, 0x9E),
    // Add more networks as needed
}
```

### API Configuration
Update token addresses for DEX integration:

```kotlin
val TOKEN_ADDRESSES = mapOf(
    "USDT" to "0xdAC17F958D2ee523a2206206994597C13D831ec7",
    "USDC" to "0xA0b86a33E6441227Aaf438b1B3b4Bb3c6f99e10D",
    "LTC" to "0x6F87D756DAf0503d08Eb8993686c7Fc01Dc44fB1",
    "DOGE" to "0x4206931337dc273a630d328dA6441786BfaD668f"
)
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Security Testing
- **Static Analysis**: Detekt for code quality
- **Dependency Scanning**: OWASP dependency check
- **Penetration Testing**: Manual security verification

## 📋 Roadmap

### Upcoming Features
- [ ] Multi-signature wallet support
- [ ] Hardware wallet integration (Ledger, Trezor)
- [ ] DeFi yield farming integration
- [ ] Cross-chain bridge support
- [ ] Advanced charting and analytics
- [ ] Social recovery mechanisms

### Security Enhancements
- [ ] Formal security audit
- [ ] Bug bounty program
- [ ] Advanced threat detection
- [ ] Secure enclave integration
- [ ] Zero-knowledge proofs

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This is educational software for demonstration purposes. Do not use with real funds without proper security audits. The developers are not responsible for any loss of funds.

## 🆘 Support

- **Documentation**: [Wiki](https://github.com/yourusername/mobile-wallet/wiki)
- **Issues**: [GitHub Issues](https://github.com/yourusername/mobile-wallet/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/mobile-wallet/discussions)
- **Security**: security@mobilewallet.com

## 🙏 Acknowledgments

- [BitcoinJ](https://bitcoinj.org/) for cryptocurrency utilities
- [BouncyCastle](https://www.bouncycastle.org/) for cryptographic functions
- [CoinGecko](https://www.coingecko.com/) for price data
- [0x Protocol](https://0x.org/) for DEX integration
- [Material Design](https://material.io/) for design guidelines

---

Built with ❤️ using Kotlin and Jetpack Compose