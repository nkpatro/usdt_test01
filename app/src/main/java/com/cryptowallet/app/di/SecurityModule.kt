package com.cryptowallet.app.di

import android.content.Context
import com.cryptowallet.app.core.crypto.WalletGenerator
import com.cryptowallet.app.core.security.CryptoWalletBiometricManager
import com.cryptowallet.app.core.security.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context
    ): SecureStorage {
        return SecureStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideWalletGenerator(): WalletGenerator {
        return WalletGenerator()
    }
    
    @Provides
    @Singleton
    fun provideBiometricManager(
        @ApplicationContext context: Context
    ): CryptoWalletBiometricManager {
        return CryptoWalletBiometricManager(context)
    }
}