package com.cryptowallet.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Handles biometric authentication for the crypto wallet
 */
class CryptoWalletBiometricManager(private val context: Context) {
    
    enum class BiometricStatus {
        SUCCESS,
        ERROR_HW_UNAVAILABLE,
        ERROR_NO_HARDWARE,
        ERROR_NONE_ENROLLED,
        ERROR_SECURITY_UPDATE_REQUIRED,
        ERROR_UNSUPPORTED,
        ERROR_STATUS_UNKNOWN
    }
    
    /**
     * Checks if biometric authentication is available on the device
     */
    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.SUCCESS
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.ERROR_HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.ERROR_NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.ERROR_NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.ERROR_SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.ERROR_UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.ERROR_STATUS_UNKNOWN
            else -> BiometricStatus.ERROR_STATUS_UNKNOWN
        }
    }
    
    /**
     * Checks if biometric authentication is available and ready to use
     */
    fun isBiometricAvailable(): Boolean {
        return checkBiometricStatus() == BiometricStatus.SUCCESS
    }
    
    /**
     * Initiates biometric authentication
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Use your biometric credential to authenticate",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onCancel()
                        else -> onError(errString.toString())
                    }
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Please try again.")
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Authenticates with both biometric and device credential fallback
     */
    fun authenticateWithDeviceCredential(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use biometric or device credential to authenticate",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED -> onCancel()
                        else -> onError(errString.toString())
                    }
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Please try again.")
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Gets a user-friendly message for biometric status
     */
    fun getBiometricStatusMessage(status: BiometricStatus): String {
        return when (status) {
            BiometricStatus.SUCCESS -> "Biometric authentication is available"
            BiometricStatus.ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricStatus.ERROR_NO_HARDWARE -> "No biometric hardware available on this device"
            BiometricStatus.ERROR_NONE_ENROLLED -> "No biometric credentials enrolled. Please set up biometric authentication in Settings"
            BiometricStatus.ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication"
            BiometricStatus.ERROR_UNSUPPORTED -> "Biometric authentication is not supported on this device"
            BiometricStatus.ERROR_STATUS_UNKNOWN -> "Unknown biometric status"
        }
    }
}