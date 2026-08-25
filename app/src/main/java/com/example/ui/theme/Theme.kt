package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CopperAccent,
    onPrimary = DarkCanvas,
    primaryContainer = CopperAccentContainerDark,
    onPrimaryContainer = CopperAccentContainerLight,
    secondary = CraftsmanAmberLight,
    onSecondary = DarkCanvas,
    secondaryContainer = AnvilBronzeContainerDark,
    onSecondaryContainer = AnvilBronzeContainerLight,
    tertiary = CraftsmanAmber,
    onTertiary = DarkCanvas,
    tertiaryContainer = DarkSurfaceContainerHigh,
    onTertiaryContainer = OnDarkText,
    background = DarkCanvas,
    onBackground = OnDarkText,
    surface = DarkSurface,
    onSurface = OnDarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkTextVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = CraftsmanAmber,
    onPrimary = LightSurface,
    primaryContainer = CopperAccentContainerLight,
    onPrimaryContainer = CraftsmanAmberDark,
    secondary = AnvilBronze,
    onSecondary = LightSurface,
    secondaryContainer = AnvilBronzeContainerLight,
    onSecondaryContainer = AnvilBronze,
    tertiary = CopperAccent,
    onTertiary = LightSurface,
    tertiaryContainer = LightSurfaceContainerHigh,
    onTertiaryContainer = OnLightText,
    background = LightCanvas,
    onBackground = OnLightText,
    surface = LightSurface,
    onSurface = OnLightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightTextVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
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
