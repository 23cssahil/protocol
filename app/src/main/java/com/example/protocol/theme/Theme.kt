package com.example.protocol.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProtocolLightColors = lightColorScheme(
    primary = Color(0xFF7C2D12),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFED7AA),
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFF3F4F6),
    outline = Color(0xFFD1D5DB)
)

@Composable
fun ProtocolTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProtocolLightColors,
        typography = Typography,
        content = content
    )
}
