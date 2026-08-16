package com.apptime.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.apptime.data.AppTheme

fun getColorScheme(theme: AppTheme) = when (theme) {
    AppTheme.DARK -> darkColorScheme(
        background = Color(0xFF1A1A2E),
        surface = Color(0xFF16213E),
        surfaceVariant = Color(0xFF1E1E3A),
        primary = Color(0xFF7B68EE),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2D2060),
        onPrimaryContainer = Color(0xFFD0CAFF),
        secondary = Color(0xFF9B84FF),
        onBackground = Color(0xFFE8E8F0),
        onSurface = Color(0xFFE8E8F0),
        outline = Color(0xFF44446A)
    )
    AppTheme.LIGHT -> lightColorScheme(
        background = Color(0xFFF8F9FA),
        surface = Color.White,
        surfaceVariant = Color(0xFFF0F0FF),
        primary = Color(0xFF6200EE),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005E),
        secondary = Color(0xFF3700B3),
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        outline = Color(0xFFCCCCCC)
    )
    AppTheme.AMOLED -> darkColorScheme(
        background = Color.Black,
        surface = Color(0xFF0D0D0D),
        surfaceVariant = Color(0xFF111111),
        primary = Color(0xFFBB86FC),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF21003E),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFF03DAC6),
        onBackground = Color(0xFFE0E0E0),
        onSurface = Color(0xFFE0E0E0),
        outline = Color(0xFF333333)
    )
    AppTheme.OCEAN -> darkColorScheme(
        background = Color(0xFF0A1628),
        surface = Color(0xFF102040),
        surfaceVariant = Color(0xFF152A50),
        primary = Color(0xFF00BCD4),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF004D5C),
        onPrimaryContainer = Color(0xFFB3E5FC),
        secondary = Color(0xFF0288D1),
        onBackground = Color(0xFFB3E5FC),
        onSurface = Color(0xFFB3E5FC),
        outline = Color(0xFF1E4070)
    )
    AppTheme.FOREST -> darkColorScheme(
        background = Color(0xFF0D1F0D),
        surface = Color(0xFF1A3A1A),
        surfaceVariant = Color(0xFF1E4A1E),
        primary = Color(0xFF4CAF50),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF1B5E20),
        onPrimaryContainer = Color(0xFFC8E6C9),
        secondary = Color(0xFF81C784),
        onBackground = Color(0xFFC8E6C9),
        onSurface = Color(0xFFC8E6C9),
        outline = Color(0xFF2D5A2D)
    )
    AppTheme.SUNSET -> darkColorScheme(
        background = Color(0xFF1A0A2E),
        surface = Color(0xFF2D1040),
        surfaceVariant = Color(0xFF3A1550),
        primary = Color(0xFFCE93D8),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF4A0060),
        onPrimaryContainer = Color(0xFFE1BEE7),
        secondary = Color(0xFFFF80AB),
        onBackground = Color(0xFFE1BEE7),
        onSurface = Color(0xFFE1BEE7),
        outline = Color(0xFF5C2080)
    )
}
