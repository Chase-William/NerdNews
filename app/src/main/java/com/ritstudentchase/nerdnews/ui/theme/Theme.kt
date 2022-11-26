package com.ritstudentchase.nerdnews.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val DarkColorPalette = darkColorScheme(
    primary = Color(0xFFF3A712),
    onPrimary = Color(0xFFC03221),
    primaryContainer = Color(0xFFD4F4DD),
    onPrimaryContainer = Color(0xFFC03221),
    background = Color(0xFF02020A)
)
// For light theme
val LightColorPalette = lightColorScheme(
    primary = Color(0xFFDDFFF7),
    onPrimary = Color(0xFF93E1D8),
    primaryContainer = Color(0xFFFFA69E),
    onPrimaryContainer = Color(0xFFAA4465),
    background = Color(0xFF462255)
)

@Composable
fun NerdNewsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    val systemUIController = rememberSystemUiController()
    systemUIController.setStatusBarColor(scheme.primary)

    MaterialTheme(
        colorScheme = scheme,
//        typography = Typography,
//        shapes = Shapes,
        content = content
    )
}