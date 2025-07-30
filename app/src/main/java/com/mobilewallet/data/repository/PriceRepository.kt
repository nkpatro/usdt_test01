package com.mobilewallet.data.repository

import com.mobilewallet.data.api.CoinGeckoApi
import com.mobilewallet.data.api.CoinPrice
import com.mobilewallet.data.api.CoinGeckoMarketResponse
import com.mobilewallet.data.model.Cryptocurrency
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result wrapper for API calls
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Data class for price update
 */
data class PriceUpdate(
    val cryptoId: String,
    val price: BigDecimal,
    val priceChange24h: Double,
    val marketCap: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Repository for managing cryptocurrency price data with real-time updates
 */
@Singleton
class PriceRepository @Inject constructor() {
    
    private val coinGeckoApi: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }
    
    // Cache for storing latest prices
    private val priceCache = ConcurrentHashMap<String, PriceUpdate>()
    
    // Flow for real-time price updates
    private val _priceUpdates = MutableSharedFlow<Map<String, PriceUpdate>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val priceUpdates: SharedFlow<Map<String, PriceUpdate>> = _priceUpdates.asSharedFlow()
    
    // Flow for error states
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()
    
    // Job for periodic price updates
    private var priceUpdateJob: Job? = null
    
    companion object {
        private const val UPDATE_INTERVAL_SECONDS = 30L
        private const val RETRY_DELAY_SECONDS = 5L
        private const val MAX_RETRIES = 3
        
        // Supported cryptocurrency IDs for CoinGecko API
        val SUPPORTED_CRYPTO_IDS = mapOf(
            "bitcoin" to "BTC",
            "litecoin" to "LTC", 
            "dogecoin" to "DOGE",
            "tether" to "USDT",
            "ethereum" to "ETH"
        )
    }
    
    /**
     * Start periodic price updates
     */
    fun startPriceUpdates(scope: CoroutineScope) {
        priceUpdateJob?.cancel()
        priceUpdateJob = scope.launch {
            while (isActive) {
                try {
                    fetchLatestPrices()
                    delay(UPDATE_INTERVAL_SECONDS * 1000)
                } catch (e: Exception) {
                    _errors.emit("Failed to update prices: ${e.message}")
                    delay(RETRY_DELAY_SECONDS * 1000)
                }
            }
        }
    }
    
    /**
     * Stop periodic price updates
     */
    fun stopPriceUpdates() {
        priceUpdateJob?.cancel()
        priceUpdateJob = null
    }
    
    /**
     * Fetch latest prices for all supported cryptocurrencies
     */
    suspend fun fetchLatestPrices(): Result<Map<String, PriceUpdate>> = withContext(Dispatchers.IO) {
        try {
            val cryptoIds = SUPPORTED_CRYPTO_IDS.keys.joinToString(",")
            val response = coinGeckoApi.getSimplePrices(
                ids = cryptoIds,
                include24hrChange = true,
                includeMarketCap = true,
                includeLastUpdated = true
            )
            
            if (response.isSuccessful) {
                val prices = response.body() ?: emptyMap()
                val updates = prices.mapNotNull { (id, price) ->
                    createPriceUpdate(id, price)?.let { update ->
                        id to update
                    }
                }.toMap()
                
                // Update cache
                priceCache.putAll(updates)
                
                // Emit updates
                _priceUpdates.emit(updates)
                
                Result.Success(updates)
            } else {
                val error = Exception("API Error: ${response.code()} ${response.message()}")
                _errors.emit("Failed to fetch prices: ${error.message}")
                Result.Error(error)
            }
        } catch (e: Exception) {
            _errors.emit("Network error: ${e.message}")
            Result.Error(e)
        }
    }
    
    /**
     * Fetch market data for supported cryptocurrencies
     */
    suspend fun fetchMarketData(): Result<List<CoinGeckoMarketResponse>> = withContext(Dispatchers.IO) {
        try {
            val cryptoIds = SUPPORTED_CRYPTO_IDS.keys.joinToString(",")
            val response = coinGeckoApi.getMarketData(ids = cryptoIds)
            
            if (response.isSuccessful) {
                val marketData = response.body() ?: emptyList()
                Result.Success(marketData)
            } else {
                val error = Exception("API Error: ${response.code()} ${response.message()}")
                Result.Error(error)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get cached price for a specific cryptocurrency
     */
    fun getCachedPrice(cryptoId: String): PriceUpdate? {
        return priceCache[cryptoId]
    }
    
    /**
     * Get all cached prices
     */
    fun getAllCachedPrices(): Map<String, PriceUpdate> {
        return priceCache.toMap()
    }
    
    /**
     * Fetch price for a specific cryptocurrency with retry logic
     */
    suspend fun fetchPriceWithRetry(
        cryptoId: String,
        maxRetries: Int = MAX_RETRIES
    ): Result<PriceUpdate> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val response = coinGeckoApi.getSimplePrices(
                    ids = cryptoId,
                    include24hrChange = true,
                    includeMarketCap = true,
                    includeLastUpdated = true
                )
                
                if (response.isSuccessful) {
                    val prices = response.body() ?: emptyMap()
                    val coinPrice = prices[cryptoId]
                    
                    if (coinPrice != null) {
                        val update = createPriceUpdate(cryptoId, coinPrice)
                        if (update != null) {
                            priceCache[cryptoId] = update
                            return@withContext Result.Success(update)
                        }
                    }
                }
                
                lastException = Exception("No price data found for $cryptoId")
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(RETRY_DELAY_SECONDS * 1000 * (attempt + 1)) // Exponential backoff
                }
            }
        }
        
        Result.Error(lastException ?: Exception("Unknown error"))
    }
    
    /**
     * Update cryptocurrency list with latest prices
     */
    suspend fun updateCryptocurrencyPrices(
        cryptocurrencies: List<Cryptocurrency>
    ): List<Cryptocurrency> = withContext(Dispatchers.Default) {
        
        val priceUpdates = getAllCachedPrices()
        
        cryptocurrencies.map { crypto ->
            val priceUpdate = priceUpdates[crypto.id]
            if (priceUpdate != null) {
                crypto.copy(
                    price = priceUpdate.price,
                    priceChange24h = priceUpdate.priceChange24h,
                    lastUpdated = priceUpdate.lastUpdated
                )
            } else {
                crypto
            }
        }
    }
    
    /**
     * Create price update from CoinGecko API response
     */
    private fun createPriceUpdate(cryptoId: String, coinPrice: CoinPrice): PriceUpdate? {
        return try {
            PriceUpdate(
                cryptoId = cryptoId,
                price = BigDecimal.valueOf(coinPrice.usd),
                priceChange24h = coinPrice.usd24hChange ?: 0.0,
                marketCap = coinPrice.usdMarketCap?.toLong(),
                lastUpdated = coinPrice.lastUpdatedAt ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create OkHttp client with logging
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "MobileWallet/1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    
    /**
     * Check if API is available
     */
    suspend fun checkApiStatus(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = coinGeckoApi.ping()
            if (response.isSuccessful) {
                Result.Success(true)
            } else {
                Result.Error(Exception("API ping failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Flow that emits price updates for a specific cryptocurrency
     */
    fun getPriceUpdatesForCrypto(cryptoId: String): Flow<PriceUpdate?> {
        return priceUpdates.map { updates ->
            updates[cryptoId]
        }.distinctUntilChanged()
    }
    
    /**
     * Flow that combines multiple cryptocurrency price updates
     */
    fun getPriceUpdatesForCryptos(cryptoIds: List<String>): Flow<Map<String, PriceUpdate>> {
        return priceUpdates.map { updates ->
            updates.filterKeys { it in cryptoIds }
        }.distinctUntilChanged()
    }
}