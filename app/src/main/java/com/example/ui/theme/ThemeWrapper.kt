package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeTokens = staticCompositionLocalOf { ThemeTokens }

/**
 * Centralized theme wrapper component that encapsulates the 'Sleek' design system,
 * applying the custom color palette, modern typography, and design tokens consistently
 * across the entire application structure.
 */
@Composable
fun ThemeWrapper(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeTokens provides ThemeTokens
    ) {
        AdventHeartsTheme(
            darkTheme = darkTheme,
            content = content
        )
    }
}
