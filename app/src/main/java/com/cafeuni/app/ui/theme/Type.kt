package com.cafeuni.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografía predeterminada de Material 3
val baseline = Typography()

/**
 * Configuración de la tipografía adaptada de material-theme (2).zip para Café Uni.
 */
val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = FontFamily.SansSerif),
    displayMedium = baseline.displayMedium.copy(fontFamily = FontFamily.SansSerif),
    displaySmall = baseline.displaySmall.copy(fontFamily = FontFamily.SansSerif),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    titleLarge = baseline.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    bodySmall = baseline.bodySmall.copy(fontFamily = FontFamily.SansSerif),
    labelLarge = baseline.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    labelMedium = baseline.labelMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
    labelSmall = baseline.labelSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
)

val AppTypography = Typography
