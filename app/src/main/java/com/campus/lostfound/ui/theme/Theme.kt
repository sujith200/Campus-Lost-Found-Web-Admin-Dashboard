package com.campus.lostfound.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color.White,
    secondary = Color(0xFF0F766E),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
)

@Composable
fun CampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
