package com.apptime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.apptime.data.AppTheme

@Composable
fun AppTimeTheme(theme: AppTheme = AppTheme.DARK, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = getColorScheme(theme),
        typography = AppTypography,
        content = content
    )
}
