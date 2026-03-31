package com.example.haven.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// iOS Haven color scheme - Primary Orange #FF9300
private val DarkColorScheme = darkColorScheme(
    primary = HavenOrangeDark,
    onPrimary = Color.White,
    primaryContainer = HavenOrangeDark.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,
    secondary = HavenOrangeDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = HavenOrangeDark,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = LabelPrimaryDark,
    surface = SurfaceDark,
    onSurface = LabelPrimaryDark,
    surfaceVariant = SurfaceDark.copy(alpha = 0.8f),
    onSurfaceVariant = LabelSecondaryDark,
    outline = LabelSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = HavenOrangeLight,
    onPrimary = Color.White,
    primaryContainer = HavenOrangeLight.copy(alpha = 0.12f),
    onPrimaryContainer = HavenOrangeHighContrast,
    secondary = HavenOrangeLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryLight,
    tertiary = HavenOrangeLight,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = LabelPrimaryLight,
    surface = BackgroundLight,
    onSurface = LabelPrimaryLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = LabelSecondaryLight,
    outline = LabelSecondaryLight
)

@Composable
fun HavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to use Haven brand colors (iOS ultramarine)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
