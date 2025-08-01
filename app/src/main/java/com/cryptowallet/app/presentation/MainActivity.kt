package com.cryptowallet.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.cryptowallet.app.presentation.navigation.CryptoWalletNavigation
import com.cryptowallet.app.presentation.theme.CryptoWalletTheme
import com.cryptowallet.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CryptoWalletTheme {
                CryptoWalletApp()
            }
        }
    }
}

@Composable
fun CryptoWalletApp(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        mainViewModel.initializeApp()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading -> {
                LoadingScreen()
            }
            !uiState.isWalletInitialized -> {
                OnboardingFlow(
                    navController = navController,
                    onWalletCreated = { mainViewModel.onWalletCreated() }
                )
            }
            uiState.requiresAuthentication -> {
                AuthenticationScreen(
                    onAuthenticationSuccess = { mainViewModel.onAuthenticationSuccess() },
                    onBiometricAuthRequested = { mainViewModel.requestBiometricAuth() }
                )
            }
            else -> {
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    CryptoWalletNavigation(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    // Loading screen implementation
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun OnboardingFlow(
    navController: androidx.navigation.NavController,
    onWalletCreated: () -> Unit
) {
    // Onboarding flow implementation will be detailed later
    com.cryptowallet.app.presentation.screens.onboarding.OnboardingScreen(
        onWalletCreated = onWalletCreated
    )
}

@Composable
fun AuthenticationScreen(
    onAuthenticationSuccess: () -> Unit,
    onBiometricAuthRequested: () -> Unit
) {
    // Authentication screen implementation will be detailed later
    com.cryptowallet.app.presentation.screens.auth.AuthenticationScreen(
        onAuthenticationSuccess = onAuthenticationSuccess,
        onBiometricAuthRequested = onBiometricAuthRequested
    )
}

@Composable
fun BottomNavigationBar(
    navController: androidx.navigation.NavController
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    
    androidx.compose.material3.NavigationBar {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Portfolio,
            BottomNavItem.Market,
            BottomNavItem.Settings
        )
        
        items.forEach { item ->
            androidx.compose.material3.NavigationBarItem(
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = { androidx.compose.material3.Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home : BottomNavItem(
        "home",
        androidx.compose.material.icons.Icons.Default.Home,
        "Home"
    )
    object Portfolio : BottomNavItem(
        "portfolio",
        androidx.compose.material.icons.Icons.Default.AccountBalanceWallet,
        "Portfolio"
    )
    object Market : BottomNavItem(
        "market",
        androidx.compose.material.icons.Icons.Default.TrendingUp,
        "Market"
    )
    object Settings : BottomNavItem(
        "settings",
        androidx.compose.material.icons.Icons.Default.Settings,
        "Settings"
    )
}