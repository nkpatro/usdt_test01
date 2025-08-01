package com.cryptowallet.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CryptoWalletApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global configurations here
        initializeLogging()
        initializeNetworkSettings()
    }
    
    private fun initializeLogging() {
        // Set up logging for debug builds
        if (BuildConfig.DEBUG) {
            // Enable detailed logging for development
        }
    }
    
    private fun initializeNetworkSettings() {
        // Configure network timeout and retry policies
        // This will be used by Retrofit and other network components
    }
}