package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
  primary = JarvisCyan,
  onPrimary = JarvisBackground,
  primaryContainer = JarvisSurfaceCard,
  onPrimaryContainer = JarvisCyan,
  secondary = JarvisBlue,
  onSecondary = Color.White,
  secondaryContainer = JarvisSurfaceVariant,
  onSecondaryContainer = JarvisTextPrimary,
  tertiary = JarvisTeal,
  onTertiary = JarvisBackground,
  background = JarvisBackground,
  onBackground = JarvisTextPrimary,
  surface = JarvisSurface,
  onSurface = JarvisTextPrimary,
  surfaceVariant = JarvisSurfaceVariant,
  onSurfaceVariant = JarvisTextSecondary,
  outline = JarvisBorder,
  outlineVariant = JarvisSurfaceCard,
  error = JarvisError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = JarvisColorScheme,
    typography = Typography,
    content = content
  )
}
