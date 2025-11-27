package com.blockforge.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D5C),
    onSecondaryContainer = BrandSecondary,
    tertiary = Pink80,
    onTertiary = Color.Black,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = Color(0xFF0D0D12),
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDarkElevated,
    surfaceContainerHigh = SurfaceDarkCard,
    surfaceContainerHighest = Color(0xFF2D2D3A),
    error = StatusInactive,
    onError = Color.White,
    outline = Color(0xFF3D3D4D),
    outlineVariant = Color(0xFF2A2A36)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF0097A7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF3F8),
    onSecondaryContainer = Color(0xFF00363D),
    tertiary = Pink40,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCFCFF),
    surfaceContainer = Color(0xFFF5F5FA),
    surfaceContainerHigh = Color(0xFFEFEFF5),
    surfaceContainerHighest = Color(0xFFE8E8F0),
    error = StatusInactive,
    onError = Color.White,
    outline = Color(0xFFD0D0E0),
    outlineVariant = Color(0xFFE5E5F0)
)

@Composable
fun BlockForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use our custom theme
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
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BlockForgeTypography,
        content = content
    )
}
