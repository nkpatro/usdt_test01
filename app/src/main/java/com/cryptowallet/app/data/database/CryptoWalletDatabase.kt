package com.cryptowallet.app.data.database

import androidx.room.*
import com.cryptowallet.app.data.database.dao.*
import com.cryptowallet.app.data.database.entities.*

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        AddressBookEntity::class,
        PriceAlertEntity::class,
        MarketDataEntity::class,
        SwapHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CryptoWalletDatabase : RoomDatabase() {
    
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun addressBookDao(): AddressBookDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun marketDataDao(): MarketDataDao
    abstract fun swapHistoryDao(): SwapHistoryDao
}

class Converters {
    
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus {
        return TransactionStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromPriceAlertType(value: PriceAlertType): String {
        return value.name
    }
    
    @TypeConverter
    fun toPriceAlertType(value: String): PriceAlertType {
        return PriceAlertType.valueOf(value)
    }
    
    @TypeConverter
    fun fromSwapStatus(value: SwapStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toSwapStatus(value: String): SwapStatus {
        return SwapStatus.valueOf(value)
    }
}