package com.sanket_satpute_20.ironmind.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepBackground,
    primaryContainer = NeonCyanMuted,
    onPrimaryContainer = NeonCyan,
    secondary = IronRed,
    onSecondary = TextPrimary,
    secondaryContainer = IronRedMuted,
    onSecondaryContainer = IronRed,
    tertiary = ElectricViolet,
    onTertiary = TextPrimary,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = BorderGlass,
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = Color(0x33FF2A55),
    onErrorContainer = ErrorRed
)

@Composable
fun IronMindTheme(
    // Always dark — IronMind is a night-mode-first premium app
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = DeepBackground.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = DeepBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
