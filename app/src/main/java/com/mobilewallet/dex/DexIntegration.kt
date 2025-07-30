package com.mobilewallet.dex

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for 0x API quote request
 */
data class ZeroXQuoteRequest(
    val sellToken: String,
    val buyToken: String,
    val sellAmount: String? = null,
    val buyAmount: String? = null,
    val takerAddress: String? = null,
    val slippagePercentage: Double = 0.01, // 1% default slippage
    val excludedSources: List<String> = emptyList(),
    val enableSlippageProtection: Boolean = true
)

/**
 * Data class for 0x API quote response
 */
data class ZeroXQuoteResponse(
    val chainId: Int,
    val price: String,
    val guaranteedPrice: String? = null,
    val estimatedPrice: String? = null,
    val to: String,
    val data: String,
    val value: String,
    val gas: String,
    val gasPrice: String,
    val buyAmount: String,
    val sellAmount: String,
    val buyTokenAddress: String,
    val sellTokenAddress: String,
    val allowanceTarget: String? = null,
    val decodedUniqueId: String? = null,
    val estimatedGas: String? = null,
    val minimumProtocolFee: String? = null,
    val sources: List<ZeroXSource> = emptyList()
)

/**
 * Data class for 0x liquidity sources
 */
data class ZeroXSource(
    val name: String,
    val proportion: String
)

/**
 * Data class for swap transaction details
 */
data class SwapTransaction(
    val id: String,
    val fromToken: String,
    val toToken: String,
    val fromAmount: BigDecimal,
    val toAmount: BigDecimal,
    val minimumToAmount: BigDecimal,
    val exchangeRate: BigDecimal,
    val slippageTolerance: Double,
    val gasFee: BigDecimal,
    val protocolFee: BigDecimal,
    val totalFee: BigDecimal,
    val estimatedTime: Long, // in seconds
    val expirationTime: Long,
    val userAddress: String,
    val status: SwapStatus = SwapStatus.PENDING,
    val txHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum for swap transaction status
 */
enum class SwapStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    EXPIRED,
    CANCELLED
}

/**
 * Security verification result
 */
data class SecurityVerification(
    val isValid: Boolean,
    val risks: List<SecurityRisk> = emptyList(),
    val recommendations: List<String> = emptyList()
)

/**
 * Security risk levels
 */
enum class SecurityRisk(val level: String, val description: String) {
    HIGH_SLIPPAGE("High", "Slippage tolerance is higher than recommended"),
    UNUSUAL_GAS("Medium", "Gas price is unusually high or low"),
    UNKNOWN_TOKEN("High", "One or more tokens are not verified"),
    LARGE_AMOUNT("Medium", "Transaction amount is unusually large"),
    UNTRUSTED_DEX("High", "DEX source is not in the trusted list"),
    PRICE_IMPACT("Medium", "Price impact is significant")
}

/**
 * 0x Protocol API interface
 */
interface ZeroXApi {
    @GET("swap/v1/quote")
    suspend fun getQuote(
        @Query("sellToken") sellToken: String,
        @Query("buyToken") buyToken: String,
        @Query("sellAmount") sellAmount: String? = null,
        @Query("buyAmount") buyAmount: String? = null,
        @Query("takerAddress") takerAddress: String? = null,
        @Query("slippagePercentage") slippagePercentage: Double = 0.01,
        @Query("excludedSources") excludedSources: String? = null,
        @Query("enableSlippageProtection") enableSlippageProtection: Boolean = true
    ): Response<ZeroXQuoteResponse>
    
    @GET("swap/v1/price")
    suspend fun getPrice(
        @Query("sellToken") sellToken: String,
        @Query("buyToken") buyToken: String,
        @Query("sellAmount") sellAmount: String? = null,
        @Query("buyAmount") buyAmount: String? = null
    ): Response<ZeroXQuoteResponse>
    
    @GET("swap/v1/sources")
    suspend fun getSources(): Response<List<String>>
}

/**
 * DEX Integration service with security considerations
 */
@Singleton
class DexIntegrationService @Inject constructor() {
    
    private val zeroXApi: ZeroXApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.0x.org/")
            .client(createSecureHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZeroXApi::class.java)
    }
    
    // Trusted DEX sources for security
    private val trustedSources = setOf(
        "Uniswap_V2",
        "Uniswap_V3", 
        "SushiSwap",
        "PancakeSwap",
        "Curve",
        "Balancer"
    )
    
    // Maximum allowed slippage (5%)
    private val maxSlippagePercentage = 0.05
    
    // Maximum transaction value for enhanced security ($10,000 USD)
    private val maxTransactionValueUsd = BigDecimal("10000")
    
    companion object {
        // Common token addresses (mainnet)
        val TOKEN_ADDRESSES = mapOf(
            "USDT" to "0xdAC17F958D2ee523a2206206994597C13D831ec7",
            "USDC" to "0xA0b86a33E6441227Aaf438b1B3b4Bb3c6f99e10D",
            "LTC" to "0x6F87D756DAf0503d08Eb8993686c7Fc01Dc44fB1", // Wrapped LTC
            "DOGE" to "0x4206931337dc273a630d328dA6441786BfaD668f", // Wrapped DOGE
            "ETH" to "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE" // Native ETH
        )
    }
    
    /**
     * Get a quote for token swap with security validation
     */
    suspend fun getSwapQuote(
        fromToken: String,
        toToken: String,
        amount: BigDecimal,
        userAddress: String,
        slippageTolerance: Double = 0.01
    ): Result<SwapTransaction> = withContext(Dispatchers.IO) {
        try {
            // Security pre-checks
            val securityCheck = validateSwapSecurity(fromToken, toToken, amount, slippageTolerance)
            if (!securityCheck.isValid) {
                return@withContext Result.failure(
                    SecurityException("Security validation failed: ${securityCheck.risks.joinToString { it.description }}")
                )
            }
            
            val fromTokenAddress = TOKEN_ADDRESSES[fromToken] ?: fromToken
            val toTokenAddress = TOKEN_ADDRESSES[toToken] ?: toToken
            val sellAmount = amount.multiply(BigDecimal("1000000000000000000")).toBigInteger().toString() // Convert to wei
            
            val response = zeroXApi.getQuote(
                sellToken = fromTokenAddress,
                buyToken = toTokenAddress,
                sellAmount = sellAmount,
                takerAddress = userAddress,
                slippagePercentage = slippageTolerance
            )
            
            if (response.isSuccessful) {
                val quote = response.body()!!
                
                // Additional security validation on the quote
                val quoteValidation = validateQuoteResponse(quote, securityCheck)
                if (!quoteValidation.isValid) {
                    return@withContext Result.failure(
                        SecurityException("Quote validation failed: ${quoteValidation.risks.joinToString { it.description }}")
                    )
                }
                
                val swapTransaction = createSwapTransaction(
                    quote = quote,
                    fromToken = fromToken,
                    toToken = toToken,
                    userAddress = userAddress,
                    slippageTolerance = slippageTolerance
                )
                
                Result.success(swapTransaction)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get real-time price for token pair
     */
    suspend fun getTokenPrice(
        fromToken: String,
        toToken: String,
        amount: BigDecimal
    ): Result<BigDecimal> = withContext(Dispatchers.IO) {
        try {
            val fromTokenAddress = TOKEN_ADDRESSES[fromToken] ?: fromToken
            val toTokenAddress = TOKEN_ADDRESSES[toToken] ?: toToken
            val sellAmount = amount.multiply(BigDecimal("1000000000000000000")).toBigInteger().toString()
            
            val response = zeroXApi.getPrice(
                sellToken = fromTokenAddress,
                buyToken = toTokenAddress,
                sellAmount = sellAmount
            )
            
            if (response.isSuccessful) {
                val quote = response.body()!!
                val price = BigDecimal(quote.price)
                Result.success(price)
            } else {
                Result.failure(Exception("Price fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate swap security parameters
     */
    private fun validateSwapSecurity(
        fromToken: String,
        toToken: String,
        amount: BigDecimal,
        slippageTolerance: Double
    ): SecurityVerification {
        val risks = mutableListOf<SecurityRisk>()
        val recommendations = mutableListOf<String>()
        
        // Check slippage tolerance
        if (slippageTolerance > maxSlippagePercentage) {
            risks.add(SecurityRisk.HIGH_SLIPPAGE)
            recommendations.add("Consider reducing slippage tolerance to below 5%")
        }
        
        // Check for unknown tokens
        if (fromToken !in TOKEN_ADDRESSES && !isValidTokenAddress(fromToken)) {
            risks.add(SecurityRisk.UNKNOWN_TOKEN)
            recommendations.add("Verify that the from-token is legitimate")
        }
        
        if (toToken !in TOKEN_ADDRESSES && !isValidTokenAddress(toToken)) {
            risks.add(SecurityRisk.UNKNOWN_TOKEN)
            recommendations.add("Verify that the to-token is legitimate")
        }
        
        // Check transaction size (assuming USD value approximation)
        if (amount > maxTransactionValueUsd) {
            risks.add(SecurityRisk.LARGE_AMOUNT)
            recommendations.add("Consider splitting large transactions for better security")
        }
        
        val isValid = risks.none { it.level == "High" }
        
        return SecurityVerification(
            isValid = isValid,
            risks = risks,
            recommendations = recommendations
        )
    }
    
    /**
     * Validate the quote response for additional security
     */
    private fun validateQuoteResponse(
        quote: ZeroXQuoteResponse,
        initialCheck: SecurityVerification
    ): SecurityVerification {
        val additionalRisks = mutableListOf<SecurityRisk>()
        val additionalRecommendations = mutableListOf<String>()
        
        // Check gas price reasonableness
        val gasPrice = BigDecimal(quote.gasPrice)
        if (gasPrice > BigDecimal("100000000000")) { // > 100 gwei
            additionalRisks.add(SecurityRisk.UNUSUAL_GAS)
            additionalRecommendations.add("Gas price is unusually high, consider waiting for lower fees")
        }
        
        // Check for trusted sources
        val untrustedSources = quote.sources.filter { it.name !in trustedSources }
        if (untrustedSources.isNotEmpty()) {
            additionalRisks.add(SecurityRisk.UNTRUSTED_DEX)
            additionalRecommendations.add("Some liquidity sources are not verified: ${untrustedSources.joinToString { it.name }}")
        }
        
        // Calculate price impact
        val price = BigDecimal(quote.price)
        val guaranteedPrice = quote.guaranteedPrice?.let { BigDecimal(it) }
        if (guaranteedPrice != null) {
            val priceImpact = price.subtract(guaranteedPrice).divide(price, 4, BigDecimal.ROUND_HALF_UP)
            if (priceImpact > BigDecimal("0.03")) { // > 3% price impact
                additionalRisks.add(SecurityRisk.PRICE_IMPACT)
                additionalRecommendations.add("High price impact detected (${priceImpact.multiply(BigDecimal("100"))}%)")
            }
        }
        
        val allRisks = initialCheck.risks + additionalRisks
        val allRecommendations = initialCheck.recommendations + additionalRecommendations
        val isValid = allRisks.none { it.level == "High" }
        
        return SecurityVerification(
            isValid = isValid,
            risks = allRisks,
            recommendations = allRecommendations
        )
    }
    
    /**
     * Create swap transaction from quote response
     */
    private fun createSwapTransaction(
        quote: ZeroXQuoteResponse,
        fromToken: String,
        toToken: String,
        userAddress: String,
        slippageTolerance: Double
    ): SwapTransaction {
        val fromAmount = BigDecimal(quote.sellAmount).divide(BigDecimal("1000000000000000000"), 18, BigDecimal.ROUND_HALF_UP)
        val toAmount = BigDecimal(quote.buyAmount).divide(BigDecimal("1000000000000000000"), 18, BigDecimal.ROUND_HALF_UP)
        val exchangeRate = toAmount.divide(fromAmount, 8, BigDecimal.ROUND_HALF_UP)
        val minimumToAmount = toAmount.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(slippageTolerance)))
        
        val gasFee = BigDecimal(quote.gas).multiply(BigDecimal(quote.gasPrice))
            .divide(BigDecimal("1000000000000000000"), 8, BigDecimal.ROUND_HALF_UP)
        
        val protocolFee = quote.minimumProtocolFee?.let { 
            BigDecimal(it).divide(BigDecimal("1000000000000000000"), 8, BigDecimal.ROUND_HALF_UP)
        } ?: BigDecimal.ZERO
        
        val totalFee = gasFee.add(protocolFee)
        
        return SwapTransaction(
            id = generateTransactionId(userAddress, fromToken, toToken),
            fromToken = fromToken,
            toToken = toToken,
            fromAmount = fromAmount,
            toAmount = toAmount,
            minimumToAmount = minimumToAmount,
            exchangeRate = exchangeRate,
            slippageTolerance = slippageTolerance,
            gasFee = gasFee,
            protocolFee = protocolFee,
            totalFee = totalFee,
            estimatedTime = 180, // 3 minutes estimate
            expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10),
            userAddress = userAddress
        )
    }
    
    /**
     * Execute the swap transaction
     */
    suspend fun executeSwap(
        swapTransaction: SwapTransaction,
        userConfirmation: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Require user confirmation for security
            if (!userConfirmation) {
                return@withContext Result.failure(
                    SecurityException("User confirmation required for swap execution")
                )
            }
            
            // Check if transaction has expired
            if (System.currentTimeMillis() > swapTransaction.expirationTime) {
                return@withContext Result.failure(
                    Exception("Transaction has expired, please request a new quote")
                )
            }
            
            // In a real implementation, this would:
            // 1. Create and sign the blockchain transaction
            // 2. Submit to the network
            // 3. Monitor for confirmation
            // 4. Update transaction status
            
            // For this example, we'll simulate successful execution
            val mockTxHash = generateMockTransactionHash()
            Result.success(mockTxHash)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Monitor swap transaction status
     */
    fun monitorSwapStatus(txHash: String): Flow<SwapStatus> = flow {
        // In a real implementation, this would poll the blockchain
        // for transaction status updates
        emit(SwapStatus.PENDING)
        delay(30000) // 30 seconds
        emit(SwapStatus.CONFIRMED)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get supported tokens for swapping
     */
    suspend fun getSupportedTokens(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Return our supported tokens for now
            Result.success(TOKEN_ADDRESSES.keys.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods
    private fun createSecureHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Don't log bodies for security
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private fun isValidTokenAddress(address: String): Boolean {
        return address.matches(Regex("^0x[a-fA-F0-9]{40}$"))
    }
    
    private fun generateTransactionId(userAddress: String, fromToken: String, toToken: String): String {
        val input = "$userAddress$fromToken$toToken${System.currentTimeMillis()}"
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }
    
    private fun generateMockTransactionHash(): String {
        return "0x" + (1..64).map { "0123456789abcdef".random() }.joinToString("")
    }
}

/**
 * User confirmation dialog data
 */
data class SwapConfirmation(
    val swapTransaction: SwapTransaction,
    val securityVerification: SecurityVerification,
    val estimatedNetworkFee: BigDecimal,
    val estimatedTotalCost: BigDecimal,
    val priceImpact: Double,
    val isHighRisk: Boolean
) {
    /**
     * Generate confirmation summary for user review
     */
    fun generateConfirmationSummary(): List<String> {
        return buildList {
            add("Swapping ${swapTransaction.fromAmount} ${swapTransaction.fromToken} for ~${swapTransaction.toAmount} ${swapTransaction.toToken}")
            add("Exchange rate: 1 ${swapTransaction.fromToken} = ${swapTransaction.exchangeRate} ${swapTransaction.toToken}")
            add("Network fee: $estimatedNetworkFee ETH")
            add("Slippage tolerance: ${(swapTransaction.slippageTolerance * 100)}%")
            
            if (securityVerification.risks.isNotEmpty()) {
                add("⚠️ Security considerations:")
                securityVerification.risks.forEach { risk ->
                    add("  • ${risk.description}")
                }
            }
            
            if (securityVerification.recommendations.isNotEmpty()) {
                add("💡 Recommendations:")
                securityVerification.recommendations.forEach { rec ->
                    add("  • $rec")
                }
            }
        }
    }
}