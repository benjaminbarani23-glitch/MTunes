package com.mtunes.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MTunesDarkColorScheme = darkColorScheme(
    primary = CyberGreen,
    onPrimary = PureBlack,
    primaryContainer = CyberGreenDim,
    onPrimaryContainer = CyberGreenLight,
    secondary = CyberGreenDark,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = PureBlack,
    outline = DarkBorder
)

@Composable
fun MTunesTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureBlack.toArgb()
            window.navigationBarColor = PureBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = MTunesDarkColorScheme,
        typography = MTunesTypography,
        content = content
    )
}
