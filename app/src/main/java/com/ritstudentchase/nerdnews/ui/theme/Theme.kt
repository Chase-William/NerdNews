package com.ritstudentchase.nerdnews.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val DarkColorPalette = darkColorScheme(
    onSurface = Color(0xFFE8E9F3),
    surface = Color(0xFF394648)
)
// For light theme
val LightColorPalette = lightColorScheme(
    onSurface = Color(0xFF394648),
    surface = Color(0xFFE8E9F3)
)

@Composable
fun NerdNewsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    val systemUIController = rememberSystemUiController()
    systemUIController.setStatusBarColor(scheme.surface)

    MaterialTheme(
        colorScheme = scheme,
//        typography = Typography,
//        shapes = Shapes,
        content = content
    )
}