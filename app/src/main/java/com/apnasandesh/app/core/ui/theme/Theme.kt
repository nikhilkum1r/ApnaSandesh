package com.apnasandesh.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandBluePrimaryLight,
    onPrimary = BrandBlueOnPrimaryLight,
    primaryContainer = BrandBluePrimaryContainerLight,
    onPrimaryContainer = BrandBlueOnPrimaryContainerLight,
    secondary = BrandTealSecondaryLight,
    onSecondary = BrandTealOnSecondaryLight,
    secondaryContainer = BrandTealSecondaryContainerLight,
    onSecondaryContainer = BrandTealOnSecondaryContainerLight,
    tertiary = BrandAmberTertiaryLight,
    onTertiary = BrandAmberOnTertiaryLight,
    tertiaryContainer = BrandAmberTertiaryContainerLight,
    onTertiaryContainer = BrandAmberOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = StatusError,
    onError = BrandBlueOnPrimaryLight,
    errorContainer = StatusErrorContainer,
    onErrorContainer = StatusError
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBluePrimaryDark,
    onPrimary = BrandBlueOnPrimaryDark,
    primaryContainer = BrandBluePrimaryContainerDark,
    onPrimaryContainer = BrandBlueOnPrimaryContainerDark,
    secondary = BrandTealSecondaryDark,
    onSecondary = BrandTealOnSecondaryDark,
    secondaryContainer = BrandTealSecondaryContainerDark,
    onSecondaryContainer = BrandTealOnSecondaryContainerDark,
    tertiary = BrandAmberTertiaryDark,
    onTertiary = BrandAmberOnTertiaryDark,
    tertiaryContainer = BrandAmberTertiaryContainerDark,
    onTertiaryContainer = BrandAmberOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = StatusErrorDark,
    onError = BrandBlueOnPrimaryDark,
    errorContainer = StatusErrorContainerDark,
    onErrorContainer = StatusErrorDark
)

@Composable
fun ApnaSandeshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BizSmsTypography,
        shapes = BizSmsShapes,
        content = content
    )
}

@Composable
fun BizSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ApnaSandeshTheme(darkTheme = darkTheme, content = content)
}
