package com.mobilewallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Enum representing different transaction types
 */
enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
    UNKNOWN
}

/**
 * Enum representing transaction status
 */
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}

/**
 * Data class representing a blockchain transaction
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val hash: String,                          // Transaction hash/ID
    val fromAddress: String,                   // Sender address
    val toAddress: String,                     // Recipient address
    val amount: BigDecimal,                    // Transaction amount
    val fee: BigDecimal,                       // Transaction fee
    val timestamp: Long,                       // Transaction timestamp
    val cryptoId: String,                      // Associated cryptocurrency ID
    val cryptoSymbol: String,                  // Cryptocurrency symbol (e.g., "BTC", "LTC")
    val type: TransactionType,                 // Transaction type
    val status: TransactionStatus = TransactionStatus.PENDING, // Transaction status
    val confirmations: Int = 0,                // Number of confirmations
    val blockHeight: Long? = null,             // Block height where transaction was included
    val gasPrice: BigDecimal? = null,          // Gas price (for Ethereum-based tokens)
    val gasUsed: BigDecimal? = null,           // Gas used (for Ethereum-based tokens)
    val memo: String? = null,                  // Optional memo/note
    val explorerUrl: String? = null            // URL to view transaction in blockchain explorer
) {
    /**
     * Check if transaction is confirmed (has enough confirmations)
     */
    fun isConfirmed(requiredConfirmations: Int = 3): Boolean {
        return confirmations >= requiredConfirmations && status == TransactionStatus.CONFIRMED
    }

    /**
     * Format amount for display with appropriate decimal places
     */
    fun getFormattedAmount(): String {
        return "${amount.stripTrailingZeros().toPlainString()} $cryptoSymbol"
    }

    /**
     * Format fee for display
     */
    fun getFormattedFee(): String {
        return "${fee.stripTrailingZeros().toPlainString()} $cryptoSymbol"
    }

    /**
     * Get formatted timestamp
     */
    fun getFormattedTimestamp(): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    /**
     * Get transaction direction for the current user
     * @param userAddress The current user's address
     */
    fun getTransactionDirection(userAddress: String): TransactionType {
        return when {
            fromAddress.equals(userAddress, ignoreCase = true) -> TransactionType.SEND
            toAddress.equals(userAddress, ignoreCase = true) -> TransactionType.RECEIVE
            else -> type
        }
    }

    /**
     * Get the other party's address (counterparty)
     * @param userAddress The current user's address
     */
    fun getCounterpartyAddress(userAddress: String): String {
        return when {
            fromAddress.equals(userAddress, ignoreCase = true) -> toAddress
            toAddress.equals(userAddress, ignoreCase = true) -> fromAddress
            else -> "Unknown"
        }
    }

    /**
     * Format short address for display (shows first 6 and last 4 characters)
     */
    fun getShortHash(): String {
        return if (hash.length > 10) {
            "${hash.take(6)}...${hash.takeLast(4)}"
        } else {
            hash
        }
    }
}