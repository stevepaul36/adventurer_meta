package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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
    onPrimary = TextPrimary,
    primaryContainer = AdventureOrangeLight,
    onPrimaryContainer = DarkBackground,
    secondary = AmberGoldDark,
    onSecondary = TextPrimary,
    secondaryContainer = AmberGoldLight,
    onSecondaryContainer = DarkBackground,
    tertiary = MountainStream,
    onTertiary = TextPrimary,
    background = TextPrimary,
    onBackground = DarkBackground,
    surface = Color(0xFFF1F5F9),
    onSurface = DarkBackground,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = DarkSurfaceVariant,
    outline = Color(0xFFCBD5E1),
    error = DangerRed,
    onError = TextPrimary
)

@Composable
fun AdventurerTheme(
    darkTheme: Boolean = true, // Default to sleek outdoor dark mode
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
