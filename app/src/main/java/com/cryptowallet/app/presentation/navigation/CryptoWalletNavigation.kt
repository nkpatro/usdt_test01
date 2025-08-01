package com.cryptowallet.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cryptowallet.app.presentation.screens.home.HomeScreen
import com.cryptowallet.app.presentation.screens.portfolio.PortfolioScreen
import com.cryptowallet.app.presentation.screens.market.MarketScreen
import com.cryptowallet.app.presentation.screens.settings.SettingsScreen
import com.cryptowallet.app.presentation.screens.send.SendScreen
import com.cryptowallet.app.presentation.screens.receive.ReceiveScreen
import com.cryptowallet.app.presentation.screens.swap.SwapScreen
import com.cryptowallet.app.presentation.screens.assetdetail.AssetDetailScreen

object Routes {
    const val HOME = "home"
    const val PORTFOLIO = "portfolio"
    const val MARKET = "market"
    const val SETTINGS = "settings"
    const val SEND = "send"
    const val RECEIVE = "receive"
    const val SWAP = "swap"
    const val ASSET_DETAIL = "asset_detail"
    const val ONBOARDING = "onboarding"
    const val CREATE_WALLET = "create_wallet"
    const val IMPORT_WALLET = "import_wallet"
    const val BACKUP_WALLET = "backup_wallet"
    const val QR_SCANNER = "qr_scanner"
    const val TRANSACTION_DETAIL = "transaction_detail"
    const val ADDRESS_BOOK = "address_book"
    const val PRICE_ALERTS = "price_alerts"
    const val SECURITY_SETTINGS = "security_settings"
}

@Composable
fun CryptoWalletNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        // Main bottom navigation screens
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSend = { coinSymbol ->
                    navController.navigate("${Routes.SEND}?coinSymbol=$coinSymbol")
                },
                onNavigateToReceive = { coinSymbol ->
                    navController.navigate("${Routes.RECEIVE}?coinSymbol=$coinSymbol")
                },
                onNavigateToSwap = {
                    navController.navigate(Routes.SWAP)
                },
                onNavigateToAssetDetail = { coinSymbol ->
                    navController.navigate("${Routes.ASSET_DETAIL}/$coinSymbol")
                }
            )
        }
        
        composable(Routes.PORTFOLIO) {
            PortfolioScreen(
                onNavigateToAssetDetail = { coinSymbol ->
                    navController.navigate("${Routes.ASSET_DETAIL}/$coinSymbol")
                },
                onNavigateToTransaction = { txHash ->
                    navController.navigate("${Routes.TRANSACTION_DETAIL}/$txHash")
                }
            )
        }
        
        composable(Routes.MARKET) {
            MarketScreen(
                onNavigateToAssetDetail = { coinSymbol ->
                    navController.navigate("${Routes.ASSET_DETAIL}/$coinSymbol")
                },
                onNavigateToPriceAlerts = {
                    navController.navigate(Routes.PRICE_ALERTS)
                }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToSecurity = {
                    navController.navigate(Routes.SECURITY_SETTINGS)
                },
                onNavigateToBackup = {
                    navController.navigate(Routes.BACKUP_WALLET)
                },
                onNavigateToAddressBook = {
                    navController.navigate(Routes.ADDRESS_BOOK)
                }
            )
        }
        
        // Transaction screens
        composable("${Routes.SEND}?coinSymbol={coinSymbol}") { backStackEntry ->
            val coinSymbol = backStackEntry.arguments?.getString("coinSymbol")
            SendScreen(
                coinSymbol = coinSymbol,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQrScanner = {
                    navController.navigate("${Routes.QR_SCANNER}?type=address")
                },
                onNavigateToAddressBook = {
                    navController.navigate(Routes.ADDRESS_BOOK)
                }
            )
        }
        
        composable("${Routes.RECEIVE}?coinSymbol={coinSymbol}") { backStackEntry ->
            val coinSymbol = backStackEntry.arguments?.getString("coinSymbol")
            ReceiveScreen(
                coinSymbol = coinSymbol,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.SWAP) {
            SwapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Detail screens
        composable("${Routes.ASSET_DETAIL}/{coinSymbol}") { backStackEntry ->
            val coinSymbol = backStackEntry.arguments?.getString("coinSymbol") ?: ""
            AssetDetailScreen(
                coinSymbol = coinSymbol,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSend = {
                    navController.navigate("${Routes.SEND}?coinSymbol=$coinSymbol")
                },
                onNavigateToReceive = {
                    navController.navigate("${Routes.RECEIVE}?coinSymbol=$coinSymbol")
                },
                onNavigateToSwap = {
                    navController.navigate(Routes.SWAP)
                }
            )
        }
        
        composable("${Routes.TRANSACTION_DETAIL}/{txHash}") { backStackEntry ->
            val txHash = backStackEntry.arguments?.getString("txHash") ?: ""
            TransactionDetailScreen(
                txHash = txHash,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Utility screens
        composable("${Routes.QR_SCANNER}?type={type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "address"
            QrScannerScreen(
                scanType = type,
                onNavigateBack = { navController.popBackStack() },
                onQrCodeScanned = { result ->
                    // Handle QR code result based on type
                    navController.previousBackStackEntry?.savedStateHandle?.set("qr_result", result)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.ADDRESS_BOOK) {
            AddressBookScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddressSelected = { address ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_address", address)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.PRICE_ALERTS) {
            PriceAlertsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings screens
        composable(Routes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.BACKUP_WALLET) {
            BackupWalletScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Placeholder composables that will be implemented later
@Composable
fun TransactionDetailScreen(
    txHash: String,
    onNavigateBack: () -> Unit
) {
    // Implementation will be added later
}

@Composable
fun QrScannerScreen(
    scanType: String,
    onNavigateBack: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    // Implementation will be added later
}

@Composable
fun AddressBookScreen(
    onNavigateBack: () -> Unit,
    onAddressSelected: (String) -> Unit
) {
    // Implementation will be added later
}

@Composable
fun PriceAlertsScreen(
    onNavigateBack: () -> Unit
) {
    // Implementation will be added later
}

@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit
) {
    // Implementation will be added later
}

@Composable
fun BackupWalletScreen(
    onNavigateBack: () -> Unit
) {
    // Implementation will be added later
}