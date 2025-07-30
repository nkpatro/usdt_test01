package com.mobilewallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enum for supported currencies
 */
enum class Currency(val symbol: String, val name: String) {
    USD("$", "US Dollar"),
    EUR("€", "Euro"),
    GBP("£", "British Pound"),
    JPY("¥", "Japanese Yen"),
    BTC("₿", "Bitcoin")
}

/**
 * Enum for app themes
 */
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Enum for security levels
 */
enum class SecurityLevel {
    LOW,      // PIN only
    MEDIUM,   // PIN + Biometric
    HIGH      // PIN + Biometric + Additional confirmation for large transactions
}

/**
 * Data class representing user settings and preferences
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1, // Single settings record
    
    // Display preferences
    val currency: Currency = Currency.USD,
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: String = "en",
    val hideBalances: Boolean = false,
    val showPriceChange: Boolean = true,
    
    // Security settings
    val securityLevel: SecurityLevel = SecurityLevel.MEDIUM,
    val biometricEnabled: Boolean = false,
    val pinRequired: Boolean = true,
    val autoLockTimeoutMinutes: Int = 5,
    val requireConfirmationForLargeTransactions: Boolean = true,
    val largeTransactionThresholdUsd: Double = 1000.0,
    
    // Notification settings
    val priceAlertsEnabled: Boolean = true,
    val transactionNotificationsEnabled: Boolean = true,
    val newsNotificationsEnabled: Boolean = false,
    
    // Network settings
    val preferredRpcNodes: Map<String, String> = emptyMap(), // crypto_id -> rpc_url
    val useTestnet: Boolean = false,
    
    // Backup settings
    val backupReminderEnabled: Boolean = true,
    val lastBackupTimestamp: Long? = null,
    val seedPhraseBackedUp: Boolean = false,
    
    // Transaction settings
    val defaultGasPrice: String = "medium", // low, medium, high
    val autoCalculateFees: Boolean = true,
    val confirmationsRequired: Map<String, Int> = mapOf( // crypto_id -> confirmations
        "bitcoin" to 3,
        "litecoin" to 6,
        "dogecoin" to 6
    ),
    
    // Advanced settings
    val enableAdvancedFeatures: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get formatted currency symbol
     */
    fun getCurrencySymbol(): String = currency.symbol

    /**
     * Check if auto-lock is enabled
     */
    fun isAutoLockEnabled(): Boolean = autoLockTimeoutMinutes > 0

    /**
     * Get auto-lock timeout in milliseconds
     */
    fun getAutoLockTimeoutMs(): Long = autoLockTimeoutMinutes * 60 * 1000L

    /**
     * Check if transaction amount requires additional confirmation
     */
    fun requiresConfirmation(amountUsd: Double): Boolean {
        return requireConfirmationForLargeTransactions && amountUsd >= largeTransactionThresholdUsd
    }

    /**
     * Get required confirmations for a specific cryptocurrency
     */
    fun getRequiredConfirmations(cryptoId: String): Int {
        return confirmationsRequired[cryptoId] ?: 3
    }

    /**
     * Get RPC node URL for a specific cryptocurrency
     */
    fun getRpcNode(cryptoId: String): String? {
        return preferredRpcNodes[cryptoId]
    }

    /**
     * Update settings with new values
     */
    fun update(
        currency: Currency = this.currency,
        theme: AppTheme = this.theme,
        language: String = this.language,
        hideBalances: Boolean = this.hideBalances,
        showPriceChange: Boolean = this.showPriceChange,
        securityLevel: SecurityLevel = this.securityLevel,
        biometricEnabled: Boolean = this.biometricEnabled,
        pinRequired: Boolean = this.pinRequired,
        autoLockTimeoutMinutes: Int = this.autoLockTimeoutMinutes,
        requireConfirmationForLargeTransactions: Boolean = this.requireConfirmationForLargeTransactions,
        largeTransactionThresholdUsd: Double = this.largeTransactionThresholdUsd,
        priceAlertsEnabled: Boolean = this.priceAlertsEnabled,
        transactionNotificationsEnabled: Boolean = this.transactionNotificationsEnabled,
        newsNotificationsEnabled: Boolean = this.newsNotificationsEnabled,
        preferredRpcNodes: Map<String, String> = this.preferredRpcNodes,
        useTestnet: Boolean = this.useTestnet,
        backupReminderEnabled: Boolean = this.backupReminderEnabled,
        lastBackupTimestamp: Long? = this.lastBackupTimestamp,
        seedPhraseBackedUp: Boolean = this.seedPhraseBackedUp,
        defaultGasPrice: String = this.defaultGasPrice,
        autoCalculateFees: Boolean = this.autoCalculateFees,
        confirmationsRequired: Map<String, Int> = this.confirmationsRequired,
        enableAdvancedFeatures: Boolean = this.enableAdvancedFeatures,
        analyticsEnabled: Boolean = this.analyticsEnabled,
        crashReportingEnabled: Boolean = this.crashReportingEnabled
    ): UserSettings {
        return this.copy(
            currency = currency,
            theme = theme,
            language = language,
            hideBalances = hideBalances,
            showPriceChange = showPriceChange,
            securityLevel = securityLevel,
            biometricEnabled = biometricEnabled,
            pinRequired = pinRequired,
            autoLockTimeoutMinutes = autoLockTimeoutMinutes,
            requireConfirmationForLargeTransactions = requireConfirmationForLargeTransactions,
            largeTransactionThresholdUsd = largeTransactionThresholdUsd,
            priceAlertsEnabled = priceAlertsEnabled,
            transactionNotificationsEnabled = transactionNotificationsEnabled,
            newsNotificationsEnabled = newsNotificationsEnabled,
            preferredRpcNodes = preferredRpcNodes,
            useTestnet = useTestnet,
            backupReminderEnabled = backupReminderEnabled,
            lastBackupTimestamp = lastBackupTimestamp,
            seedPhraseBackedUp = seedPhraseBackedUp,
            defaultGasPrice = defaultGasPrice,
            autoCalculateFees = autoCalculateFees,
            confirmationsRequired = confirmationsRequired,
            enableAdvancedFeatures = enableAdvancedFeatures,
            analyticsEnabled = analyticsEnabled,
            crashReportingEnabled = crashReportingEnabled,
            updatedAt = System.currentTimeMillis()
        )
    }
}