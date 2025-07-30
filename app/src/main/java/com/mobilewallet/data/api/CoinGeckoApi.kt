package com.mobilewallet.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

/**
 * Data class for CoinGecko price response
 */
data class CoinGeckoPriceResponse(
    @SerializedName("bitcoin")
    val bitcoin: CoinPrice? = null,
    @SerializedName("litecoin")
    val litecoin: CoinPrice? = null,
    @SerializedName("dogecoin")
    val dogecoin: CoinPrice? = null,
    @SerializedName("tether")
    val tether: CoinPrice? = null
)

/**
 * Data class for individual coin price information
 */
data class CoinPrice(
    @SerializedName("usd")
    val usd: Double,
    @SerializedName("usd_24h_change")
    val usd24hChange: Double? = null,
    @SerializedName("usd_market_cap")
    val usdMarketCap: Double? = null,
    @SerializedName("last_updated_at")
    val lastUpdatedAt: Long? = null
)

/**
 * Data class for CoinGecko market data response
 */
data class CoinGeckoMarketResponse(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("current_price")
    val currentPrice: Double,
    @SerializedName("market_cap")
    val marketCap: Long? = null,
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
    @SerializedName("circulating_supply")
    val circulatingSupply: Double? = null,
    @SerializedName("total_supply")
    val totalSupply: Double? = null,
    @SerializedName("last_updated")
    val lastUpdated: String? = null,
    val image: String? = null
)

/**
 * Retrofit interface for CoinGecko API
 */
interface CoinGeckoApi {
    
    /**
     * Get simple prices for multiple cryptocurrencies
     */
    @GET("simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hrChange: Boolean = true,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_last_updated_at") includeLastUpdated: Boolean = true
    ): Response<Map<String, CoinPrice>>
    
    /**
     * Get market data for cryptocurrencies
     */
    @GET("coins/markets")
    suspend fun getMarketData(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): Response<List<CoinGeckoMarketResponse>>
    
    /**
     * Get supported coins list
     */
    @GET("coins/list")
    suspend fun getSupportedCoins(): Response<List<SupportedCoin>>
    
    /**
     * Ping endpoint to check API status
     */
    @GET("ping")
    suspend fun ping(): Response<PingResponse>
}

/**
 * Data class for supported coin information
 */
data class SupportedCoin(
    val id: String,
    val symbol: String,
    val name: String
)

/**
 * Data class for ping response
 */
data class PingResponse(
    @SerializedName("gecko_says")
    val geckoSays: String
)