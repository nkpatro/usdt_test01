package com.cryptowallet.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApiService {
    
    @GET("simple/price")
    suspend fun getCurrentPrices(
        @Query("ids") coinIds: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24HrChange: Boolean = true,
        @Query("include_24hr_vol") include24HrVol: Boolean = true,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_last_updated_at") includeLastUpdated: Boolean = true
    ): Response<Map<String, CoinPrice>>
    
    @GET("coins/markets")
    suspend fun getMarketData(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") coinIds: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 250,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") priceChangePercentage: String = "24h,7d"
    ): Response<List<CoinMarketData>>
    
    @GET("coins/{id}/history")
    suspend fun getHistoricalData(
        @Query("id") coinId: String,
        @Query("date") date: String, // DD-MM-YYYY format
        @Query("localization") localization: Boolean = false
    ): Response<CoinHistoricalData>
    
    @GET("search/trending")
    suspend fun getTrendingCoins(): Response<TrendingCoinsResponse>
    
    @GET("global")
    suspend fun getGlobalMarketData(): Response<GlobalMarketData>
}

@Serializable
data class CoinPrice(
    val usd: Double = 0.0,
    val usd_24h_change: Double? = null,
    val usd_24h_vol: Double? = null,
    val usd_market_cap: Double? = null,
    val last_updated_at: Long? = null
)

@Serializable
data class CoinMarketData(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val market_cap: Long?,
    val market_cap_rank: Int?,
    val fully_diluted_valuation: Long?,
    val total_volume: Long?,
    val high_24h: Double?,
    val low_24h: Double?,
    val price_change_24h: Double?,
    val price_change_percentage_24h: Double?,
    val price_change_percentage_7d_in_currency: Double?,
    val market_cap_change_24h: Double?,
    val market_cap_change_percentage_24h: Double?,
    val circulating_supply: Double?,
    val total_supply: Double?,
    val max_supply: Double?,
    val ath: Double?,
    val ath_change_percentage: Double?,
    val ath_date: String?,
    val atl: Double?,
    val atl_change_percentage: Double?,
    val atl_date: String?,
    val last_updated: String
)

@Serializable
data class CoinHistoricalData(
    val id: String,
    val symbol: String,
    val name: String,
    val market_data: HistoricalMarketData
)

@Serializable
data class HistoricalMarketData(
    val current_price: Map<String, Double>,
    val market_cap: Map<String, Long>,
    val total_volume: Map<String, Long>
)

@Serializable
data class TrendingCoinsResponse(
    val coins: List<TrendingCoin>
)

@Serializable
data class TrendingCoin(
    val item: TrendingCoinItem
)

@Serializable
data class TrendingCoinItem(
    val id: String,
    val coin_id: Int,
    val name: String,
    val symbol: String,
    val market_cap_rank: Int,
    val thumb: String,
    val small: String,
    val large: String,
    val slug: String,
    val price_btc: Double,
    val score: Int
)

@Serializable
data class GlobalMarketData(
    val data: GlobalData
)

@Serializable
data class GlobalData(
    val active_cryptocurrencies: Int,
    val upcoming_icos: Int,
    val ongoing_icos: Int,
    val ended_icos: Int,
    val markets: Int,
    val total_market_cap: Map<String, Double>,
    val total_volume: Map<String, Double>,
    val market_cap_percentage: Map<String, Double>,
    val market_cap_change_percentage_24h_usd: Double,
    val updated_at: Long
)

/**
 * Maps coin symbols to CoinGecko IDs for API calls
 */
object CoinGeckoIdMapper {
    
    private val coinIdMap = mapOf(
        "BTC" to "bitcoin",
        "ETH" to "ethereum",
        "SOL" to "solana",
        "LTC" to "litecoin",
        "DOGE" to "dogecoin",
        "LKY" to "luckycoin",
        "DGB" to "digibyte",
        "BEL" to "bellscoin",
        "JKC" to "junkcoin",
        "DINGO" to "dingocoin",
        "SHIBA" to "shibacoin",
        "CAT" to "catcoin",
        "BONKS" to "bonkscoin",
        "PEPE" to "pepecore",
        "USDT" to "tether"
    )
    
    fun getCoinGeckoId(symbol: String): String? {
        return coinIdMap[symbol.uppercase()]
    }
    
    fun getSymbolFromId(id: String): String? {
        return coinIdMap.entries.find { it.value == id }?.key
    }
    
    fun getAllSupportedIds(): String {
        return coinIdMap.values.joinToString(",")
    }
    
    fun getIdsForSymbols(symbols: List<String>): String {
        return symbols.mapNotNull { getCoinGeckoId(it) }.joinToString(",")
    }
}