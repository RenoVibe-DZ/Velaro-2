package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VelaroDarkColorScheme = darkColorScheme(
  primary = TechCyan,
  onPrimary = DeepNavy,
  primaryContainer = DeepNavySurface,
  onPrimaryContainer = TechCyanLight,
  secondary = ElectricBlue,
  onSecondary = Color.White,
  tertiary = SuccessGreen,
  onTertiary = Color.White,
  background = DeepNavy,
  onBackground = Color.White,
  surface = DeepNavySurface,
  onSurface = Color.White,
  surfaceVariant = DeepNavyBorder,
  onSurfaceVariant = TextMuted,
  outline = DeepNavyBorder
)

private val VelaroLightColorScheme = lightColorScheme(
  primary = ElectricBlue,
  onPrimary = Color.White,
  primaryContainer = TechCyanLight,
  onPrimaryContainer = DeepNavy,
  secondary = TechCyan,
  onSecondary = DeepNavy,
  tertiary = SuccessGreen,
  onTertiary = Color.White,
  background = AluminiumLight,
  onBackground = TextPrimary,
  surface = Color.White,
  onSurface = TextPrimary,
  surfaceVariant = AluminiumCard,
  onSurfaceVariant = TextSecondary,
  outline = AluminiumBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For Velaro brand identity, we prioritize our tailored automotive tech theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) VelaroDarkColorScheme else VelaroLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
