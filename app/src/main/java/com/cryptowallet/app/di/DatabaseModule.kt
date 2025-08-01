package com.cryptowallet.app.di

import android.content.Context
import androidx.room.Room
import com.cryptowallet.app.data.database.CryptoWalletDatabase
import com.cryptowallet.app.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideCryptoWalletDatabase(
        @ApplicationContext context: Context
    ): CryptoWalletDatabase {
        return Room.databaseBuilder(
            context,
            CryptoWalletDatabase::class.java,
            "crypto_wallet_database"
        )
        .fallbackToDestructiveMigration() // For development - remove in production
        .build()
    }
    
    @Provides
    fun provideWalletDao(database: CryptoWalletDatabase): WalletDao {
        return database.walletDao()
    }
    
    @Provides
    fun provideTransactionDao(database: CryptoWalletDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideAddressBookDao(database: CryptoWalletDatabase): AddressBookDao {
        return database.addressBookDao()
    }
    
    @Provides
    fun providePriceAlertDao(database: CryptoWalletDatabase): PriceAlertDao {
        return database.priceAlertDao()
    }
    
    @Provides
    fun provideMarketDataDao(database: CryptoWalletDatabase): MarketDataDao {
        return database.marketDataDao()
    }
    
    @Provides
    fun provideSwapHistoryDao(database: CryptoWalletDatabase): SwapHistoryDao {
        return database.swapHistoryDao()
    }
}