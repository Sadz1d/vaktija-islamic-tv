package com.islamictv.display.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2D6A4F),
    secondary = Color(0xFF40916C),
    tertiary = Color(0xFFFFE66D),
    background = Color(0xFF1B4332),
    surface = Color(0xFF2D6A4F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun IslamicTVDisplayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}