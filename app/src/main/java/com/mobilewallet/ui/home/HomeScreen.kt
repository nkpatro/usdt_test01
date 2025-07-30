package com.mobilewallet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mobilewallet.data.model.Cryptocurrency
import com.mobilewallet.data.model.UserSettings
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

/**
 * Data class representing the home screen UI state
 */
data class HomeUiState(
    val cryptocurrencies: List<Cryptocurrency> = emptyList(),
    val totalPortfolioValue: BigDecimal = BigDecimal.ZERO,
    val totalPortfolioChange24h: Double = 0.0,
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * Sealed class representing home screen actions
 */
sealed class HomeAction {
    data class OnCryptocurrencyClick(val cryptocurrency: Cryptocurrency) : HomeAction()
    object OnSendClick : HomeAction()
    object OnReceiveClick : HomeAction()
    object OnSwapClick : HomeAction()
    object OnRefresh : HomeAction()
    object OnSettingsClick : HomeAction()
}

/**
 * Main home screen composable that displays wallet assets and action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Wallet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { onAction(HomeAction.OnSettingsClick) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Portfolio summary section
            PortfolioSummaryCard(
                totalValue = uiState.totalPortfolioValue,
                change24h = uiState.totalPortfolioChange24h,
                userSettings = uiState.userSettings,
                currencyFormatter = currencyFormatter,
                modifier = Modifier.padding(16.dp)
            )

            // Action buttons
            ActionButtonsRow(
                onSendClick = { onAction(HomeAction.OnSendClick) },
                onReceiveClick = { onAction(HomeAction.OnReceiveClick) },
                onSwapClick = { onAction(HomeAction.OnSwapClick) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Assets list
            AssetsSection(
                cryptocurrencies = uiState.cryptocurrencies,
                userSettings = uiState.userSettings,
                onCryptocurrencyClick = { crypto ->
                    onAction(HomeAction.OnCryptocurrencyClick(crypto))
                },
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onAction(HomeAction.OnRefresh) },
                modifier = Modifier.weight(1f)
            )
        }

        // Error handling
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Portfolio summary card showing total value and 24h change
 */
@Composable
private fun PortfolioSummaryCard(
    totalValue: BigDecimal,
    change24h: Double,
    userSettings: UserSettings,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Portfolio Value",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (userSettings.hideBalances) {
                Text(
                    text = "****",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = currencyFormatter.format(totalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (userSettings.showPriceChange && !userSettings.hideBalances) {
                Spacer(modifier = Modifier.height(8.dp))

                val changeColor = if (change24h >= 0) {
                    Color(0xFF4CAF50) // Green for positive
                } else {
                    Color(0xFFF44336) // Red for negative
                }

                Text(
                    text = "${if (change24h >= 0) "+" else ""}${String.format("%.2f", change24h)}% (24h)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Row of action buttons (Send, Receive, Swap)
 */
@Composable
private fun ActionButtonsRow(
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            onClick = onSendClick,
            modifier = Modifier.weight(1f)
        )

        ActionButton(
            icon = Icons.Default.CallReceived,
            label = "Receive",
            onClick = onReceiveClick,
            modifier = Modifier.weight(1f)
        )

        ActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Swap",
            onClick = onSwapClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual action button component
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Assets section with pull-to-refresh functionality
 */
@Composable
private fun AssetsSection(
    cryptocurrencies: List<Cryptocurrency>,
    userSettings: UserSettings,
    onCryptocurrencyClick: (Cryptocurrency) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Assets list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cryptocurrencies) { crypto ->
                CryptocurrencyItem(
                    cryptocurrency = crypto,
                    userSettings = userSettings,
                    onClick = { onCryptocurrencyClick(crypto) }
                )
            }
        }
    }
}

/**
 * Individual cryptocurrency item in the list
 */
@Composable
private fun CryptocurrencyItem(
    cryptocurrency: Cryptocurrency,
    userSettings: UserSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crypto logo
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cryptocurrency.logoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${cryptocurrency.name} logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Crypto info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cryptocurrency.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${cryptocurrency.getFormattedBalance()} ${cryptocurrency.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price and change info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (userSettings.hideBalances) {
                    Text(
                        text = "****",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = cryptocurrency.getFormattedPrice(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (userSettings.showPriceChange) {
                        val changeColor = if (cryptocurrency.isPriceIncreasing) {
                            Color(0xFF4CAF50) // Green for positive
                        } else {
                            Color(0xFFF44336) // Red for negative
                        }

                        Text(
                            text = cryptocurrency.getFormattedPriceChange(),
                            style = MaterialTheme.typography.bodySmall,
                            color = changeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}