package com.mobilewallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Data class representing a cryptocurrency in the wallet
 */
@Entity(tableName = "cryptocurrencies")
data class Cryptocurrency(
    @PrimaryKey
    val id: String,                    // Unique identifier (e.g., "bitcoin", "litecoin")
    val symbol: String,                // Symbol (e.g., "BTC", "LTC", "DOGE")
    val name: String,                  // Full name (e.g., "Bitcoin", "Litecoin")
    val balance: BigDecimal,           // User's balance in this cryptocurrency
    val price: BigDecimal,             // Current price in USD
    val priceChange24h: Double,        // 24-hour price change percentage
    val logoUrl: String,               // URL to the cryptocurrency logo
    val explorerUrl: String,           // URL to the blockchain explorer
    val isEnabled: Boolean = true,     // Whether this crypto is enabled in the wallet
    val decimals: Int = 8,             // Number of decimal places
    val lastUpdated: Long = System.currentTimeMillis() // Last price update timestamp
) {
    /**
     * Calculate the USD value of the user's balance
     */
    val balanceUsd: BigDecimal
        get() = balance * price

    /**
     * Format balance for display with appropriate decimal places
     */
    fun getFormattedBalance(): String {
        return balance.setScale(decimals, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
    }

    /**
     * Format price for display
     */
    fun getFormattedPrice(): String {
        return "$${price.setScale(2, BigDecimal.ROUND_HALF_UP)}"
    }

    /**
     * Format price change with sign and percentage
     */
    fun getFormattedPriceChange(): String {
        val sign = if (priceChange24h >= 0) "+" else ""
        return "$sign${String.format("%.2f", priceChange24h)}%"
    }

    /**
     * Check if price has increased in the last 24 hours
     */
    val isPriceIncreasing: Boolean
        get() = priceChange24h > 0
}