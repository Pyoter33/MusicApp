package com.example.musicapp.presentation.theme

import android.annotation.SuppressLint
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightThemeColors = lightColors(
    primary = Blue,
    onPrimary = Black,
    secondary = LightBlue,
    onSecondary = DarkGrey,
    background = White,
    onBackground = Black,
    surface = LightBlue,
    onSurface = DarkGrey
)

private val DarkThemeColors = darkColors(
    primary = Purple,
    onPrimary = White,
    secondary = Grey,
    onSecondary = LightGrey,
    background = DarkGrey,
    onBackground = White,
    surface = Black,
    onSurface = White
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = if (isDarkTheme) DarkThemeColors else LightThemeColors) {
        content()
    }
}