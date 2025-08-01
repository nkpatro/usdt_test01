package com.cryptowallet.app.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthenticationScreen(
    onAuthenticationSuccess: () -> Unit,
    onBiometricAuthRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Authentication Required",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAuthenticationSuccess,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Authenticate with PIN")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onBiometricAuthRequested,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Use Biometric")
        }
    }
}