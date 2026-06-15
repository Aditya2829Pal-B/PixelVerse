package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
      primary = InstagramDarkOnBackground,
      background = InstagramDarkBackground,
      surface = InstagramDarkBackground,
      onPrimary = InstagramDarkBackground,
      onBackground = InstagramDarkOnBackground,
      onSurface = InstagramDarkOnBackground,
      surfaceVariant = InstagramSeparatorDark
  )

private val LightColorScheme =
  lightColorScheme(
      primary = InstagramLightOnBackground,
      background = InstagramLightBackground,
      surface = InstagramLightBackground,
      onPrimary = InstagramLightBackground,
      onBackground = InstagramLightOnBackground,
      onSurface = InstagramLightOnBackground,
      surfaceVariant = InstagramSeparator
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
