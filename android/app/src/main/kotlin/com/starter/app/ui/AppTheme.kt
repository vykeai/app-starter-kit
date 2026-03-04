package com.starter.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.starter.app.design.tokens.AppColors

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error,
    onPrimary = Color.White,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
