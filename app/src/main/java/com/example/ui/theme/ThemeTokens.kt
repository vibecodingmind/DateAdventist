package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design system tokens defining standardized border-radius values,
 * elevation/shadow levels, spacing scales, and gradient schemes for the 'Sleek' design theme.
 */
object ThemeTokens {
    // Standardized Border Radii
    val BorderRadiusSmall: Dp = 12.dp
    val BorderRadiusMedium: Dp = 16.dp
    val BorderRadiusLarge: Dp = 24.dp
    val BorderRadiusExtraLarge: Dp = 32.dp
    val BorderRadiusPill: Dp = 50.dp

    // Pre-built Component Shapes
    val ShapeSmall = RoundedCornerShape(BorderRadiusSmall)
    val ShapeMedium = RoundedCornerShape(BorderRadiusMedium)
    val ShapeLarge = RoundedCornerShape(BorderRadiusLarge)
    val ShapeExtraLarge = RoundedCornerShape(BorderRadiusExtraLarge)
    val ShapePill = CircleShape

    // Elevation & Shadow Tokens
    val ElevationNone: Dp = 0.dp
    val ElevationSubtle: Dp = 2.dp
    val ElevationMedium: Dp = 6.dp
    val ElevationHigh: Dp = 8.dp
    val ElevationHero: Dp = 12.dp

    // Spacing Scale Tokens
    val SpaceMicro: Dp = 2.dp
    val SpaceExtraSmall: Dp = 4.dp
    val SpaceSmall: Dp = 8.dp
    val SpaceMedium: Dp = 12.dp
    val SpaceLarge: Dp = 16.dp
    val SpaceExtraLarge: Dp = 24.dp
    val SpaceHuge: Dp = 32.dp

    // Colors & Gradients
    val PrimaryRoyalNavy = Color(0xFF1E3A8A)
    val PrimaryElectricBlue = Color(0xFF3B82F6)
    val AccentVibrantRose = Color(0xFFF43F5E)
    val AccentWarmGold = Color(0xFFEAB308)
    val CanvasSoftCream = Color(0xFFFAFAFC)
    val SurfacePureWhite = Color(0xFFFFFFFF)

    val NavyPrimaryGradient = Brush.linearGradient(
        colors = listOf(PrimaryRoyalNavy, PrimaryElectricBlue)
    )

    val RoseActionGradient = Brush.linearGradient(
        colors = listOf(AccentVibrantRose, Color(0xFFE11D48))
    )

    val CardDarkGradientScrim = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
    )
}
