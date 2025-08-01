package com.cryptowallet.app.core.crypto

enum class CoinType(
    val coinName: String,
    val symbol: String,
    val isScrypt: Boolean,
    val decimals: Int,
    val networkType: NetworkType,
    val derivationPath: String,
    val addressPrefix: String? = null,
    val iconRes: String
) {
    // Bitcoin (SHA-256)
    BITCOIN(
        coinName = "Bitcoin",
        symbol = "BTC",
        isScrypt = false,
        decimals = 8,
        networkType = NetworkType.BITCOIN,
        derivationPath = "m/44'/0'/0'/0",
        iconRes = "ic_bitcoin"
    ),
    
    // Ethereum
    ETHEREUM(
        coinName = "Ethereum",
        symbol = "ETH",
        isScrypt = false,
        decimals = 18,
        networkType = NetworkType.ETHEREUM,
        derivationPath = "m/44'/60'/0'/0",
        iconRes = "ic_ethereum"
    ),
    
    // Solana
    SOLANA(
        coinName = "Solana",
        symbol = "SOL",
        isScrypt = false,
        decimals = 9,
        networkType = NetworkType.SOLANA,
        derivationPath = "m/44'/501'/0'/0'",
        iconRes = "ic_solana"
    ),
    
    // Scrypt-based coins
    LITECOIN(
        coinName = "Litecoin",
        symbol = "LTC",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.LITECOIN,
        derivationPath = "m/44'/2'/0'/0",
        addressPrefix = "L",
        iconRes = "ic_litecoin"
    ),
    
    DOGECOIN(
        coinName = "Dogecoin",
        symbol = "DOGE",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.DOGECOIN,
        derivationPath = "m/44'/3'/0'/0",
        addressPrefix = "D",
        iconRes = "ic_dogecoin"
    ),
    
    LUCKYCOIN(
        coinName = "Luckycoin",
        symbol = "LKY",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.LUCKYCOIN,
        derivationPath = "m/44'/9'/0'/0",
        iconRes = "ic_luckycoin"
    ),
    
    DIGIBYTES(
        coinName = "DigiByte",
        symbol = "DGB",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.DIGIBYTES,
        derivationPath = "m/44'/20'/0'/0",
        iconRes = "ic_digibytes"
    ),
    
    BELLSCOIN(
        coinName = "Bellscoin",
        symbol = "BEL",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.BELLSCOIN,
        derivationPath = "m/44'/25'/0'/0",
        iconRes = "ic_bellscoin"
    ),
    
    JUNKCOIN(
        coinName = "Junkcoin",
        symbol = "JKC",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.JUNKCOIN,
        derivationPath = "m/44'/26'/0'/0",
        iconRes = "ic_junkcoin"
    ),
    
    DINGOCOIN(
        coinName = "Dingocoin",
        symbol = "DINGO",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.DINGOCOIN,
        derivationPath = "m/44'/28'/0'/0",
        iconRes = "ic_dingocoin"
    ),
    
    SHIBACOIN(
        coinName = "Shibacoin",
        symbol = "SHIBA",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.SHIBACOIN,
        derivationPath = "m/44'/29'/0'/0",
        iconRes = "ic_shibacoin"
    ),
    
    CATCOIN(
        coinName = "Catcoin",
        symbol = "CAT",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.CATCOIN,
        derivationPath = "m/44'/30'/0'/0",
        iconRes = "ic_catcoin"
    ),
    
    BONKSCOIN(
        coinName = "Bonkscoin",
        symbol = "BONKS",
        isScrypt = true,
        decimals = 8,
        networkType = NetworkType.BONKSCOIN,
        derivationPath = "m/44'/31'/0'/0",
        iconRes = "ic_bonkscoin"
    ),
    
    PEPECORE(
        coinName = "Pepecore",
        symbol = "PEPE",
        isScrypt = false,
        decimals = 18,
        networkType = NetworkType.PEPECORE,
        derivationPath = "m/44'/32'/0'/0",
        iconRes = "ic_pepecore"
    ),
    
    // Tokens
    USDT_ERC20(
        coinName = "Tether (ERC-20)",
        symbol = "USDT",
        isScrypt = false,
        decimals = 6,
        networkType = NetworkType.ETHEREUM,
        derivationPath = "m/44'/60'/0'/0",
        iconRes = "ic_usdt"
    );
    
    companion object {
        fun getScryptCoins() = values().filter { it.isScrypt }
        fun getNonScryptCoins() = values().filter { !it.isScrypt }
        fun getBySymbol(symbol: String) = values().find { it.symbol == symbol }
    }
}

enum class NetworkType {
    BITCOIN,
    ETHEREUM,
    SOLANA,
    LITECOIN,
    DOGECOIN,
    LUCKYCOIN,
    DIGIBYTES,
    BELLSCOIN,
    JUNKCOIN,
    DINGOCOIN,
    SHIBACOIN,
    CATCOIN,
    BONKSCOIN,
    PEPECORE
}