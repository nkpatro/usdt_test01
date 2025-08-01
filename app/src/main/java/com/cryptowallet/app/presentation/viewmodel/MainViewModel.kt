package com.cryptowallet.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptowallet.app.core.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val secureStorage: SecureStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun initializeApp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Check if wallet is initialized
                val isWalletInitialized = secureStorage.isWalletInitialized()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isWalletInitialized = isWalletInitialized,
                    requiresAuthentication = isWalletInitialized
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize app: ${e.message}"
                )
            }
        }
    }
    
    fun onWalletCreated() {
        _uiState.value = _uiState.value.copy(
            isWalletInitialized = true,
            requiresAuthentication = false
        )
    }
    
    fun onAuthenticationSuccess() {
        _uiState.value = _uiState.value.copy(
            requiresAuthentication = false
        )
    }
    
    fun requestBiometricAuth() {
        // Handle biometric authentication request
        // This will be implemented when biometric screen is ready
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val isWalletInitialized: Boolean = false,
    val requiresAuthentication: Boolean = false,
    val error: String? = null
)