package com.cryptowallet.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CryptoPrimary,
    onPrimary = CryptoOnPrimary,
    primaryContainer = CryptoPrimaryContainer,
    onPrimaryContainer = CryptoOnPrimaryContainer,
    secondary = CryptoSecondary,
    onSecondary = CryptoOnSecondary,
    secondaryContainer = CryptoSecondaryContainer,
    onSecondaryContainer = CryptoOnSecondaryContainer,
    tertiary = CryptoTertiary,
    onTertiary = CryptoOnTertiary,
    tertiaryContainer = CryptoTertiaryContainer,
    onTertiaryContainer = CryptoOnTertiaryContainer,
    error = CryptoError,
    onError = CryptoOnError,
    errorContainer = CryptoErrorContainer,
    onErrorContainer = CryptoOnErrorContainer,
    background = CryptoDarkBackground,
    onBackground = CryptoDarkOnBackground,
    surface = CryptoDarkSurface,
    onSurface = CryptoDarkOnSurface,
    surfaceVariant = CryptoDarkSurfaceVariant,
    onSurfaceVariant = CryptoDarkOnSurfaceVariant,
    outline = CryptoDarkOutline,
    outlineVariant = CryptoDarkOutlineVariant,
    scrim = CryptoScrim,
    inverseSurface = CryptoDarkInverseSurface,
    inverseOnSurface = CryptoDarkInverseOnSurface,
    inversePrimary = CryptoDarkInversePrimary,
    surfaceDim = CryptoDarkSurfaceDim,
    surfaceBright = CryptoDarkSurfaceBright,
    surfaceContainerLowest = CryptoDarkSurfaceContainerLowest,
    surfaceContainerLow = CryptoDarkSurfaceContainerLow,
    surfaceContainer = CryptoDarkSurfaceContainer,
    surfaceContainerHigh = CryptoDarkSurfaceContainerHigh,
    surfaceContainerHighest = CryptoDarkSurfaceContainerHighest,
)

private val LightColorScheme = lightColorScheme(
    primary = CryptoPrimary,
    onPrimary = CryptoOnPrimary,
    primaryContainer = CryptoPrimaryContainer,
    onPrimaryContainer = CryptoOnPrimaryContainer,
    secondary = CryptoSecondary,
    onSecondary = CryptoOnSecondary,
    secondaryContainer = CryptoSecondaryContainer,
    onSecondaryContainer = CryptoOnSecondaryContainer,
    tertiary = CryptoTertiary,
    onTertiary = CryptoOnTertiary,
    tertiaryContainer = CryptoTertiaryContainer,
    onTertiaryContainer = CryptoOnTertiaryContainer,
    error = CryptoError,
    onError = CryptoOnError,
    errorContainer = CryptoErrorContainer,
    onErrorContainer = CryptoOnErrorContainer,
    background = CryptoLightBackground,
    onBackground = CryptoLightOnBackground,
    surface = CryptoLightSurface,
    onSurface = CryptoLightOnSurface,
    surfaceVariant = CryptoLightSurfaceVariant,
    onSurfaceVariant = CryptoLightOnSurfaceVariant,
    outline = CryptoLightOutline,
    outlineVariant = CryptoLightOutlineVariant,
    scrim = CryptoScrim,
    inverseSurface = CryptoLightInverseSurface,
    inverseOnSurface = CryptoLightInverseOnSurface,
    inversePrimary = CryptoLightInversePrimary,
    surfaceDim = CryptoLightSurfaceDim,
    surfaceBright = CryptoLightSurfaceBright,
    surfaceContainerLowest = CryptoLightSurfaceContainerLowest,
    surfaceContainerLow = CryptoLightSurfaceContainerLow,
    surfaceContainer = CryptoLightSurfaceContainer,
    surfaceContainerHigh = CryptoLightSurfaceContainerHigh,
    surfaceContainerHighest = CryptoLightSurfaceContainerHighest,
)

@Composable
fun CryptoWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CryptoTypography,
        content = content
    )
}