package com.simo3000.imieicompiti.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary          = LightAccent,
    onPrimary        = LightSurface,
    background       = LightBackground,
    onBackground     = LightText1,
    surface          = LightSurface,
    onSurface        = LightText1,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightText2,
    outline          = LightBorder,
    error            = ErrorText,
)

private val DarkColors = darkColorScheme(
    primary          = DarkAccent,
    onPrimary        = DarkSurface,
    background       = DarkBackground,
    onBackground     = DarkText1,
    surface          = DarkSurface,
    onSurface        = DarkText1,
    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = DarkText2,
    outline          = DarkBorder,
    error            = ErrorText,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view   = LocalView.current

    // Solo isAppearanceLightStatusBars — enableEdgeToEdge() gestisce già il resto.
    // Impostare window.statusBarColor qui confligge con enableEdgeToEdge() e
    // causa un redraw della window ad ogni recomposition.
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        content     = content
    )
}