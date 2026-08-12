package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI Design Tokens for the Sleek Interface Theme in AdventHearts.
 */
object SleekTokens {
    // Corner Radius Tokens
    val CardCornerRadiusLarge: Dp = 32.dp
    val CardCornerRadiusMedium: Dp = 24.dp
    val CardCornerRadiusSmall: Dp = 16.dp
    val ButtonCornerRadius: Dp = 16.dp
    val InputCornerRadius: Dp = 20.dp
    val PillCornerRadius: Dp = 50.dp

    // Shapes
    val CardShapeLarge = RoundedCornerShape(CardCornerRadiusLarge)
    val CardShapeMedium = RoundedCornerShape(CardCornerRadiusMedium)
    val CardShapeSmall = RoundedCornerShape(CardCornerRadiusSmall)
    val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
    val InputShape = RoundedCornerShape(InputCornerRadius)
    val PillShape = CircleShape

    // Elevation Tokens
    val ElevationHeroCard: Dp = 12.dp
    val ElevationStandardCard: Dp = 6.dp
    val ElevationActionButton: Dp = 8.dp
    val ElevationSubtle: Dp = 2.dp

    // Glassmorphic & Border Tokens
    val BorderWidthThin: Dp = 1.dp
    val BorderColorLight = Color(0xFFE2E8F0)
    val GlassmorphicOverlayColor = Color.White.copy(alpha = 0.20f)
    val GlassmorphicBorderColor = Color.White.copy(alpha = 0.30f)

    // Gradients
    val PrimaryNavyGradient = Brush.linearGradient(
        colors = listOf(NavyPrimary, NavyBlueGradientEnd)
    )

    val RoseActionGradient = Brush.linearGradient(
        colors = listOf(AdventRedPrimary, AdventRedPrimaryDark)
    )

    val CardScrimGradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
    )
}

val SleekShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(SleekTokens.CardCornerRadiusSmall),
    medium = RoundedCornerShape(SleekTokens.CardCornerRadiusMedium),
    large = RoundedCornerShape(SleekTokens.CardCornerRadiusLarge),
    extraLarge = CircleShape
)
