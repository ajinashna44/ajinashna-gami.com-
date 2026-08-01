package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CryptoColorScheme = darkColorScheme(
    primary = CryptoGold,
    onPrimary = Color(0xFF0A0E17),
    primaryContainer = CryptoGoldDark,
    onPrimaryContainer = Color.White,
    secondary = CryptoGreen,
    onSecondary = Color(0xFF0A0E17),
    tertiary = EthColor,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = CryptoRed
)

@Composable
fun CryptoConverterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CryptoColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CryptoConverterTheme(content = content)
}
