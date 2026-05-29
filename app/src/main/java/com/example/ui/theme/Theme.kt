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

private val DarkColorScheme = darkColorScheme(
    primary = ImdbYellow,
    onPrimary = Color(0xFF000000),
    secondary = ImdbYellow,
    onSecondary = Color(0xFF000000),
    tertiary = ImdbYellow,
    background = CinematicBlack,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = TextSecondary
)

private val LightColorScheme = darkColorScheme( // Force dark theme vibe always for movie-theater immersion
    primary = ImdbYellow,
    onPrimary = Color(0xFF000000),
    secondary = ImdbYellow,
    onSecondary = Color(0xFF000000),
    tertiary = ImdbYellow,
    background = CinematicBlack,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for cinematic dark mode
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve theater styling elements
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
  )
}
