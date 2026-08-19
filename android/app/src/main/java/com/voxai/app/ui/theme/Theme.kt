package com.voxai.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark neon color scheme — always dark mode
private val VoxAIColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = NeonBlack,
    primaryContainer = NeonCyan.copy(alpha = 0.15f),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = NeonWhite,
    secondaryContainer = NeonPurple.copy(alpha = 0.15f),
    onSecondaryContainer = NeonPurple,
    tertiary = NeonBlue,
    onTertiary = NeonWhite,
    background = NeonBlack,
    onBackground = NeonWhite,
    surface = NeonSurface,
    onSurface = NeonWhite,
    surfaceVariant = NeonSurfaceVariant,
    onSurfaceVariant = NeonGray,
    outline = NeonBorder,
    outlineVariant = NeonBorder,
    error = NeonRed,
    onError = NeonWhite,
)

/** Neon gradient brush for buttons, logos, etc. */
val NeonGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(NeonCyan, NeonPurple),
    )

/** Soft neon gradient for backgrounds */
val NeonGradientSoft: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(NeonCyan.copy(alpha = 0.15f), NeonPurple.copy(alpha = 0.15f)),
    )

/** Radial glow gradient */
val NeonGlow: Brush
    @Composable get() = Brush.radialGradient(
        colors = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
    )

@Composable
fun VoxAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = VoxAIColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NeonBlack.toArgb()
            window.navigationBarColor = NeonBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoxAITypography,
        content = content,
    )
}
