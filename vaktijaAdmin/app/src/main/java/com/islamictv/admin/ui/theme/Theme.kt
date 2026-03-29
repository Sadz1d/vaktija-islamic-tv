package com.islamictv.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    secondary = IslamicGreenLight,
    tertiary = IslamicGold,
    background = IslamicBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = IslamicGreenDark,
    onSurface = IslamicGreenDark,
)

@Composable
fun IslamicTVAdminTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}