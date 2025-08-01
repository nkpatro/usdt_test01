package com.cryptowallet.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptowallet.app.core.crypto.CoinType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // Dependencies will be injected here
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // For now, create mock data for all supported coins
                val mockWallets = CoinType.values().map { coinType ->
                    WalletDisplayItem(
                        coinSymbol = coinType.symbol,
                        coinName = coinType.coinName,
                        balance = "0.00 ${coinType.symbol}",
                        balanceUsd = "$0.00"
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    wallets = mockWallets,
                    totalPortfolioValueUsd = "$0.00",
                    portfolioChange24h = "+0.00%"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }
    
    fun refreshData() {
        loadData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val wallets: List<WalletDisplayItem> = emptyList(),
    val totalPortfolioValueUsd: String = "$0.00",
    val portfolioChange24h: String = "",
    val error: String? = null
)