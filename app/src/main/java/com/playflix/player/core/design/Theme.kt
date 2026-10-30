package com.playflix.player.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PlayFlixViolet,
    secondary = PlayFlixPurple,
    tertiary = PlayFlixBlue,
    background = PlayFlixNavyDark,
    surface = PlayFlixNavySurface,
    surfaceVariant = PlayFlixCardSurface,
    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = PlayFlixBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PlayFlixViolet,
    secondary = PlayFlixPurple,
    tertiary = PlayFlixBlue,
    background = PlayFlixLightBackground,
    surface = PlayFlixLightSurface,
    surfaceVariant = PlayFlixLightCard,
    onPrimary = PlayFlixLightSurface,
    onSecondary = PlayFlixLightSurface,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = PlayFlixBorderLight
)

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

@Composable
fun PlayFlixTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography = PlayFlixTypography,
        content = content
    )
}
