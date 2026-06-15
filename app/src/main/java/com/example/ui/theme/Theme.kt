package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val PremiumDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF0C0C0E),
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    background = ObsidianBg,
    onBackground = LightTextPrimary,
    surface = ObsidianSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = LightTextSecondary,
    outline = ObsidianBorder,
    error = Color(0xFFCF6679)
)

private val PremiumLightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF0C0C0E),
    primaryContainer = GoldAccent,
    onPrimaryContainer = Color(0xFF0C0C0E),
    secondary = GoldDark,
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFECECEF),
    onSurfaceVariant = Color(0xFF6C6C75),
    outline = Color(0xFFD6D6DD),
    error = AccentDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep standard dynamicColor option but default to cohesive branded theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PremiumDarkColorScheme
        else -> PremiumLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
