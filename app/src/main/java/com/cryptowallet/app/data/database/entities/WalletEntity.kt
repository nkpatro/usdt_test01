package com.cryptowallet.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cryptowallet.app.core.crypto.CoinType
import java.math.BigDecimal

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val coinSymbol: String,
    val coinName: String,
    val address: String,
    val balance: String, // Store as string to avoid precision issues
    val balanceUsd: String = "0.0",
    val isEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val derivationPath: String,
    val addressIndex: Int = 0
) {
    fun getBalanceAsBigDecimal(): BigDecimal {
        return try {
            BigDecimal(balance)
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
    
    fun getBalanceUsdAsBigDecimal(): BigDecimal {
        return try {
            BigDecimal(balanceUsd)
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val txHash: String,
    val coinSymbol: String,
    val fromAddress: String = "",
    val toAddress: String,
    val amount: String,
    val amountUsd: String = "0.0",
    val fee: String = "0.0",
    val feeUsd: String = "0.0",
    val type: TransactionType,
    val status: TransactionStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val blockHeight: Long = 0,
    val confirmations: Int = 0,
    val note: String = "",
    val gasPrice: String = "0", // For Ethereum-based transactions
    val gasLimit: String = "0",
    val nonce: Int = 0
)

enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
    FEE
}

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED
}

@Entity(tableName = "address_book")
data class AddressBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val coinSymbol: String,
    val note: String = "",
    val isFrequent: Boolean = false,
    val lastUsed: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coinSymbol: String,
    val alertType: PriceAlertType,
    val targetPrice: String,
    val currentPrice: String = "0.0",
    val percentageChange: String = "0.0", // For percentage-based alerts
    val isEnabled: Boolean = true,
    val isTriggered: Boolean = false,
    val triggeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PriceAlertType {
    PRICE_ABOVE,
    PRICE_BELOW,
    PERCENTAGE_UP,
    PERCENTAGE_DOWN
}

@Entity(tableName = "market_data")
data class MarketDataEntity(
    @PrimaryKey
    val coinSymbol: String,
    val name: String,
    val currentPrice: String,
    val priceChangePercentage24h: String,
    val priceChangePercentage7d: String,
    val marketCap: String = "0",
    val volume24h: String = "0",
    val circulatingSupply: String = "0",
    val totalSupply: String = "0",
    val high24h: String = "0",
    val low24h: String = "0",
    val lastUpdated: Long = System.currentTimeMillis(),
    val rank: Int = 0,
    val iconUrl: String = ""
)

@Entity(tableName = "swap_history")
data class SwapHistoryEntity(
    @PrimaryKey
    val swapId: String,
    val fromCoinSymbol: String,
    val toCoinSymbol: String,
    val fromAmount: String,
    val toAmount: String,
    val exchangeRate: String,
    val swapFee: String,
    val swapFeeUsd: String = "0.0",
    val provider: String, // Exchange provider name
    val status: SwapStatus,
    val fromTxHash: String = "",
    val toTxHash: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val note: String = ""
)

enum class SwapStatus {
    INITIATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED
}