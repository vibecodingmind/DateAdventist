package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NavyBlueGradientEnd,
    onPrimary = Color.White,
    primaryContainer = NavyPrimary,
    secondary = AdventRedPrimary,
    onSecondary = Color.White,
    secondaryContainer = AdventRedPrimaryDark,
    tertiary = GoldTertiary,
    background = SurfaceDark,
    surface = CardSurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceVariant = CardSurfaceDark,
    outline = TextSecondaryDark.copy(alpha = 0.3f)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavySecondaryContainer,
    secondary = AdventRedPrimary,
    onSecondary = Color.White,
    secondaryContainer = AdventRedContainer,
    tertiary = GoldTertiary,
    background = SurfaceLight,
    surface = CardSurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceVariant = NavySecondaryContainer,
    outline = BorderLight
)

/**
 * Centralized theme wrapper for AdventHearts.
 * Applies the 'Sleek' color, typography, shape, and spacing design system across the entire application.
 */
@Composable
fun AdventHeartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our distinct brand colors for visual consistency
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SleekShapes,
        content = content
    )
}

/**
 * Convenient theme object to access Sleek Design Tokens directly in Composables.
 */
object SleekTheme {
    val tokens: SleekTokens
        @Composable
        @ReadOnlyComposable
        get() = SleekTokens

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = SleekShapes
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AdventHeartsTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
