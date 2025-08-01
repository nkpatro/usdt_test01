package com.cryptowallet.app.data.database.dao

import androidx.room.*
import com.cryptowallet.app.data.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    
    // Wallet operations
    @Query("SELECT * FROM wallets ORDER BY coinSymbol")
    fun getAllWallets(): Flow<List<WalletEntity>>
    
    @Query("SELECT * FROM wallets WHERE isEnabled = 1 ORDER BY coinSymbol")
    fun getEnabledWallets(): Flow<List<WalletEntity>>
    
    @Query("SELECT * FROM wallets WHERE coinSymbol = :coinSymbol")
    suspend fun getWallet(coinSymbol: String): WalletEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)
    
    @Update
    suspend fun updateWallet(wallet: WalletEntity)
    
    @Query("UPDATE wallets SET balance = :balance, balanceUsd = :balanceUsd, lastUpdated = :timestamp WHERE coinSymbol = :coinSymbol")
    suspend fun updateWalletBalance(coinSymbol: String, balance: String, balanceUsd: String, timestamp: Long)
    
    @Query("UPDATE wallets SET isEnabled = :isEnabled WHERE coinSymbol = :coinSymbol")
    suspend fun updateWalletEnabled(coinSymbol: String, isEnabled: Boolean)
    
    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)
    
    @Query("DELETE FROM wallets")
    suspend fun deleteAllWallets()
    
    @Query("SELECT SUM(CAST(balanceUsd AS REAL)) FROM wallets WHERE isEnabled = 1")
    suspend fun getTotalPortfolioValueUsd(): Double?
}

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE coinSymbol = :coinSymbol ORDER BY timestamp DESC")
    fun getTransactionsByCoin(coinSymbol: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE txHash = :txHash")
    suspend fun getTransaction(txHash: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY timestamp DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET status = :status, confirmations = :confirmations WHERE txHash = :txHash")
    suspend fun updateTransactionStatus(txHash: String, status: TransactionStatus, confirmations: Int)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE coinSymbol = :coinSymbol")
    suspend fun deleteTransactionsByCoin(coinSymbol: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM transactions WHERE coinSymbol = :coinSymbol AND type = :type")
    suspend fun getTransactionCountByCoinAndType(coinSymbol: String, type: TransactionType): Int
    
    @Query("SELECT SUM(CAST(amountUsd AS REAL)) FROM transactions WHERE type = :type AND timestamp >= :startTime")
    suspend fun getTotalValueByTypeAndPeriod(type: TransactionType, startTime: Long): Double?
}

@Dao
interface AddressBookDao {
    
    @Query("SELECT * FROM address_book ORDER BY name")
    fun getAllAddresses(): Flow<List<AddressBookEntity>>
    
    @Query("SELECT * FROM address_book WHERE coinSymbol = :coinSymbol ORDER BY name")
    fun getAddressesByCoin(coinSymbol: String): Flow<List<AddressBookEntity>>
    
    @Query("SELECT * FROM address_book WHERE isFrequent = 1 ORDER BY lastUsed DESC")
    fun getFrequentAddresses(): Flow<List<AddressBookEntity>>
    
    @Query("SELECT * FROM address_book WHERE id = :id")
    suspend fun getAddress(id: Long): AddressBookEntity?
    
    @Query("SELECT * FROM address_book WHERE address = :address AND coinSymbol = :coinSymbol")
    suspend fun getAddressByValue(address: String, coinSymbol: String): AddressBookEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressBookEntity): Long
    
    @Update
    suspend fun updateAddress(address: AddressBookEntity)
    
    @Query("UPDATE address_book SET lastUsed = :timestamp, isFrequent = 1 WHERE address = :address AND coinSymbol = :coinSymbol")
    suspend fun markAddressAsUsed(address: String, coinSymbol: String, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteAddress(address: AddressBookEntity)
    
    @Query("DELETE FROM address_book")
    suspend fun deleteAllAddresses()
    
    @Query("SELECT COUNT(*) FROM address_book WHERE coinSymbol = :coinSymbol")
    suspend fun getAddressCountByCoin(coinSymbol: String): Int
}

@Dao
interface PriceAlertDao {
    
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>
    
    @Query("SELECT * FROM price_alerts WHERE isEnabled = 1 AND isTriggered = 0")
    fun getActiveAlerts(): Flow<List<PriceAlertEntity>>
    
    @Query("SELECT * FROM price_alerts WHERE coinSymbol = :coinSymbol ORDER BY createdAt DESC")
    fun getAlertsByCoin(coinSymbol: String): Flow<List<PriceAlertEntity>>
    
    @Query("SELECT * FROM price_alerts WHERE id = :id")
    suspend fun getAlert(id: Long): PriceAlertEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long
    
    @Update
    suspend fun updateAlert(alert: PriceAlertEntity)
    
    @Query("UPDATE price_alerts SET isTriggered = 1, triggeredAt = :timestamp WHERE id = :id")
    suspend fun markAlertAsTriggered(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE price_alerts SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAlertEnabled(id: Long, isEnabled: Boolean)
    
    @Delete
    suspend fun deleteAlert(alert: PriceAlertEntity)
    
    @Query("DELETE FROM price_alerts WHERE coinSymbol = :coinSymbol")
    suspend fun deleteAlertsByCoin(coinSymbol: String)
    
    @Query("DELETE FROM price_alerts")
    suspend fun deleteAllAlerts()
}

@Dao
interface MarketDataDao {
    
    @Query("SELECT * FROM market_data ORDER BY rank ASC")
    fun getAllMarketData(): Flow<List<MarketDataEntity>>
    
    @Query("SELECT * FROM market_data WHERE coinSymbol = :coinSymbol")
    suspend fun getMarketData(coinSymbol: String): MarketDataEntity?
    
    @Query("SELECT * FROM market_data WHERE coinSymbol IN (:coinSymbols)")
    suspend fun getMarketDataForCoins(coinSymbols: List<String>): List<MarketDataEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketData(marketData: MarketDataEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketData(marketData: List<MarketDataEntity>)
    
    @Update
    suspend fun updateMarketData(marketData: MarketDataEntity)
    
    @Query("UPDATE market_data SET currentPrice = :price, priceChangePercentage24h = :change24h, lastUpdated = :timestamp WHERE coinSymbol = :coinSymbol")
    suspend fun updatePrice(coinSymbol: String, price: String, change24h: String, timestamp: Long)
    
    @Delete
    suspend fun deleteMarketData(marketData: MarketDataEntity)
    
    @Query("DELETE FROM market_data")
    suspend fun deleteAllMarketData()
    
    @Query("SELECT lastUpdated FROM market_data WHERE coinSymbol = :coinSymbol")
    suspend fun getLastUpdatedTime(coinSymbol: String): Long?
}

@Dao
interface SwapHistoryDao {
    
    @Query("SELECT * FROM swap_history ORDER BY timestamp DESC")
    fun getAllSwaps(): Flow<List<SwapHistoryEntity>>
    
    @Query("SELECT * FROM swap_history WHERE swapId = :swapId")
    suspend fun getSwap(swapId: String): SwapHistoryEntity?
    
    @Query("SELECT * FROM swap_history WHERE status = :status ORDER BY timestamp DESC")
    fun getSwapsByStatus(status: SwapStatus): Flow<List<SwapHistoryEntity>>
    
    @Query("SELECT * FROM swap_history WHERE fromCoinSymbol = :coinSymbol OR toCoinSymbol = :coinSymbol ORDER BY timestamp DESC")
    fun getSwapsByCoin(coinSymbol: String): Flow<List<SwapHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwap(swap: SwapHistoryEntity)
    
    @Update
    suspend fun updateSwap(swap: SwapHistoryEntity)
    
    @Query("UPDATE swap_history SET status = :status, completedAt = :completedAt WHERE swapId = :swapId")
    suspend fun updateSwapStatus(swapId: String, status: SwapStatus, completedAt: Long? = null)
    
    @Query("UPDATE swap_history SET fromTxHash = :fromTxHash, toTxHash = :toTxHash WHERE swapId = :swapId")
    suspend fun updateSwapTransactionHashes(swapId: String, fromTxHash: String, toTxHash: String)
    
    @Delete
    suspend fun deleteSwap(swap: SwapHistoryEntity)
    
    @Query("DELETE FROM swap_history")
    suspend fun deleteAllSwaps()
    
    @Query("SELECT COUNT(*) FROM swap_history WHERE status = :status")
    suspend fun getSwapCountByStatus(status: SwapStatus): Int
}