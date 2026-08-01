package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CopperAccent,
    secondary = CraftsmanAmber,
    tertiary = AnvilBronze,
    background = DarkCanvas,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkCanvas,
    onBackground = OnDarkText,
    onSurface = OnDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = CraftsmanAmber,
    secondary = AnvilBronze,
    tertiary = CopperAccent,
    background = LightCanvas,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = LightSurface,
    onBackground = OnLightText,
    onSurface = OnLightText
)

@Composable
fun HuesmithTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
