package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VidooDarkColorScheme = darkColorScheme(
  primary = VidooOrange,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF381800),
  onPrimaryContainer = VidooOrangeVariant,
  secondary = VidooOrange,
  onSecondary = Color.Black,
  secondaryContainer = VidooSurfaceVariant,
  onSecondaryContainer = VidooTextPrimary,
  tertiary = VidooOrangeVariant,
  onTertiary = Color.Black,
  background = VidooBlack,
  onBackground = VidooTextPrimary,
  surface = VidooDarkCharcoal,
  onSurface = VidooTextPrimary,
  surfaceVariant = VidooSurface,
  onSurfaceVariant = VidooTextSecondary,
  outline = VidooBorder,
  outlineVariant = Color(0xFF222228),
  error = Color(0xFFFF5252),
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Vidoo enforces modern dark mode black / dark charcoal base with orange accent
  MaterialTheme(
    colorScheme = VidooDarkColorScheme,
    typography = Typography,
    content = content
  )
}

