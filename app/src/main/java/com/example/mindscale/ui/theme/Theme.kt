package com.example.mindscale.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// UPDATED: A single, non-dynamic color scheme to match the app's specification.
private val AppColorScheme = lightColorScheme(
    primary = AppColors.Gold,
    secondary = AppColors.GrayLight,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun MindScaleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}