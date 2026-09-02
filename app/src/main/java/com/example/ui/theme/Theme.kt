package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GeoBlueContainer,
    secondary = GeoBluePrimary,
    tertiary = LedMatrixAmber,
    background = GeoDarkCanvas,
    surface = Color(0xFF202327),
    surfaceVariant = Color(0xFF2E3238),
    onPrimary = GeoOnBlueContainer,
    onSecondary = Color.White,
    onTertiary = GeoDarkCanvas,
    onBackground = GeoDarkOnSurface,
    onSurface = GeoDarkOnSurface,
    outline = Color(0xFF44474E),
    outlineVariant = Color(0xFF33363B)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GeoBluePrimary,
    onPrimary = GeoBlueOnPrimary,
    primaryContainer = GeoBlueContainer,
    onPrimaryContainer = GeoOnBlueContainer,
    secondary = GeoTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeoIconCircleBg,
    onSecondaryContainer = GeoIconCircleText,
    tertiary = GeoBluePrimary,
    onTertiary = Color.White,
    background = GeoLightCanvas,
    onBackground = GeoTextPrimary,
    surface = GeoSurfaceLight,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoTextSecondary,
    outline = GeoBorderLight,
    outlineVariant = Color(0xFFC4C7C5)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Ensure crisp custom light blue theme is always applied
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
