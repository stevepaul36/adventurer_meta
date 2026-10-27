package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AdventureOrange,
    onPrimary = DarkBackground,
    primaryContainer = AdventureOrangeDark,
    onPrimaryContainer = TextPrimary,
    secondary = AmberGold,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = AmberGoldLight,
    tertiary = HighAltitudeCyan,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF0C4A6E),
    onTertiaryContainer = HighAltitudeCyanLight,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    error = DangerRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AdventureOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE6),
    onPrimaryContainer = AdventureOrangeDark,
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = AmberGoldDark,
    tertiary = HighAltitudeCyan,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0369A1),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun AdventurerTheme(
    darkTheme: Boolean = false, // Defaults to bright daylight outdoor theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
