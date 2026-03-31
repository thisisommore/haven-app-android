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

// iOS Haven color scheme - Primary Brown #87521B
// Header background color
val HeaderBackground = Color(0xFFFFEEE2)
private val DarkColorScheme = darkColorScheme(
    primary = HavenPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A3B2A),
    onPrimaryContainer = Color(0xFFFFD9B3),
    secondary = HavenPrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D3225),
    onSecondaryContainer = Color(0xFFF5DBC8),
    tertiary = HavenPrimaryDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3D3225),
    onTertiaryContainer = Color(0xFFF5DBC8),
    background = BackgroundDark,
    onBackground = LabelPrimaryDark,
    surface = SurfaceDark,
    onSurface = LabelPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = LabelSecondaryDark,
    surfaceTint = HavenPrimaryDark.copy(alpha = 0.1f),
    inverseSurface = Color(0xFFE5E5EA),
    inverseOnSurface = Color(0xFF1C1C1E),
    inversePrimary = Color(0xFFFF9500),
    outline = Color(0x55EBEBF5),
    outlineVariant = Color(0x33EBEBF5),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3A3A3C),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainerLowest = Color(0xFF000000),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF5C1008),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = HavenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4D6),
    onPrimaryContainer = HavenPrimary,
    secondary = HavenPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0E8),
    onSecondaryContainer = HavenPrimary,
    tertiary = HavenPrimary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF0E8),
    onTertiaryContainer = HavenPrimary,
    background = BackgroundLight,
    onBackground = LabelPrimaryLight,
    surface = BackgroundLight,
    onSurface = LabelPrimaryLight,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = LabelSecondaryLight,
    surfaceTint = HavenPrimary.copy(alpha = 0.05f),
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFFF9500),
    outline = Color(0x993C3C43),
    outlineVariant = Color(0x553C3C43),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF2F2F7),
    surfaceContainerHighest = Color(0xFFE5E5EA),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF5C1008)
)

@Composable
fun HavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to use Haven brand colors
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
            window.statusBarColor = HeaderBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
