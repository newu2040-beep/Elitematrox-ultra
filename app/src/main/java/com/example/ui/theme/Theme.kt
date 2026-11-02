package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.domain.model.AppFontFamily
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePalette

@Composable
fun EliteMatrixTheme(
    themePalette: ThemePalette = ThemePalette.WarmIvory,
    themeMode: ThemeMode = ThemeMode.Light,
    fontFamily: AppFontFamily = AppFontFamily.PlusJakarta,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.Light -> themePalette.isDarkPalette
        ThemeMode.Dark -> true
        ThemeMode.System -> systemDark || themePalette.isDarkPalette
    }

    val neuColors = paletteFor(themePalette, useDark)
    val activeTypography = typographyFor(fontFamily)

    val colorScheme = if (neuColors.isDark) {
        darkColorScheme(
            primary = neuColors.accentPrimary,
            onPrimary = Color.White,
            secondary = neuColors.accentSecondary,
            background = neuColors.canvas,
            surface = neuColors.surfaceRaised,
            onBackground = neuColors.textPrimary,
            onSurface = neuColors.textPrimary,
            surfaceVariant = neuColors.surfaceInset,
            onSurfaceVariant = neuColors.textSecondary
        )
    } else {
        lightColorScheme(
            primary = neuColors.accentPrimary,
            onPrimary = Color.White,
            secondary = neuColors.accentSecondary,
            background = neuColors.canvas,
            surface = neuColors.surfaceRaised,
            onBackground = neuColors.textPrimary,
            onSurface = neuColors.textPrimary,
            surfaceVariant = neuColors.surfaceInset,
            onSurfaceVariant = neuColors.textSecondary
        )
    }

    CompositionLocalProvider(LocalNeumorphicColors provides neuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = activeTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    EliteMatrixTheme(
        themePalette = ThemePalette.WarmIvory,
        themeMode = if (darkTheme) ThemeMode.Dark else ThemeMode.Light,
        content = content
    )
}
