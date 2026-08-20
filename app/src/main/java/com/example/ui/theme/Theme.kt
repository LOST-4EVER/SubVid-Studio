package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = ImmersiveOnSecondary,
    secondaryContainer = ImmersiveSecondaryContainer,
    onSecondaryContainer = ImmersiveOnSecondaryContainer,
    tertiary = ImmersiveTertiary,
    onTertiary = ImmersiveOnTertiary,
    background = ImmersiveBg,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveBorderFocused
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF276B64),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8EEE5),
    onPrimaryContainer = Color(0xFF06201D),
    secondary = Color(0xFF526170),
    onSecondary = Color.White,
    background = Color(0xFFF7F9FA),
    onBackground = Color(0xFF161A1E),
    surface = Color(0xFFF7F9FA),
    onSurface = Color(0xFF161A1E),
    surfaceVariant = Color(0xFFE2E8EC),
    onSurfaceVariant = Color(0xFF414B54)
  )

private val StudioShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(14.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SubVidStudioTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colors = when {
    darkTheme -> DarkColorScheme
    dynamicColor -> LightColorScheme
    else -> LightColorScheme
  }
  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    shapes = StudioShapes,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  SubVidStudioTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
  )
}

