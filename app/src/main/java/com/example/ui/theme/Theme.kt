package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemePreset(val id: String, val displayName: String) {
    NOVEL_AI("novel_ai", "NovelAI Dark"),
    MIDNIGHT_PURPLE("midnight_purple", "Midnight Violet"),
    CLASSIC_SLATE("classic_slate", "Classic Slate"),
    LIGHT_MODERN("light_modern", "Light Modern"),
    CUSTOM("custom", "Custom Color Theme")
}

private val NovelAiColorScheme = darkColorScheme(
    primary = NovelAiPrimary,
    onPrimary = NovelAiOnPrimary,
    primaryContainer = NovelAiPrimaryContainer,
    onPrimaryContainer = NovelAiOnPrimaryContainer,
    secondary = NovelAiSecondary,
    secondaryContainer = NovelAiSurfaceVariant,
    onSecondaryContainer = Color.White,
    background = NovelAiBg,
    surface = NovelAiSurface,
    surfaceVariant = NovelAiSurfaceVariant,
    onBackground = Color(0xFFECEEF8),
    onSurface = Color(0xFFECEEF8),
    onSurfaceVariant = Color(0xFFBAC0DE),
    outline = NovelAiOutline,
    outlineVariant = NovelAiOutline.copy(alpha = 0.6f)
)

private val MidnightPurpleColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightPrimaryContainer,
    onPrimaryContainer = MidnightOnPrimaryContainer,
    background = MidnightBg,
    surface = MidnightSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onBackground = Color(0xFFF0EAFB),
    onSurface = Color(0xFFF0EAFB),
    onSurfaceVariant = Color(0xFFC7B8E6)
)

private val ClassicSlateColorScheme = darkColorScheme(
    primary = SlatePrimary,
    onPrimary = SlateOnPrimary,
    primaryContainer = SlatePrimaryContainer,
    onPrimaryContainer = SlateOnPrimaryContainer,
    background = SlateBg,
    surface = SlateSurface,
    surfaceVariant = SlateSurfaceVariant,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

private val LightModernColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF1E1F2A),
    onSurface = Color(0xFF1E1F2A),
    onSurfaceVariant = Color(0xFF5B607A)
)

fun createCustomColorScheme(primaryColor: Color, bgColor: Color): androidx.compose.material3.ColorScheme {
    val isLight = androidx.core.graphics.ColorUtils.calculateLuminance(bgColor.value.toLong().toInt()) > 0.5
    val onBg = if (isLight) Color(0xFF1E1F2A) else Color(0xFFECEEF8)
    val surfaceColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1B1D2E)
    val surfaceVariantColor = if (isLight) Color(0xFFEFEFF5) else Color(0xFF262940)

    return if (isLight) {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            secondaryContainer = surfaceVariantColor,
            onSecondaryContainer = onBg,
            background = bgColor,
            surface = surfaceColor,
            surfaceVariant = surfaceVariantColor,
            onBackground = onBg,
            onSurface = onBg,
            onSurfaceVariant = onBg.copy(alpha = 0.7f),
            outline = primaryColor.copy(alpha = 0.4f),
            outlineVariant = primaryColor.copy(alpha = 0.2f)
        )
    } else {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            secondaryContainer = surfaceVariantColor,
            onSecondaryContainer = Color.White,
            background = bgColor,
            surface = surfaceColor,
            surfaceVariant = surfaceVariantColor,
            onBackground = onBg,
            onSurface = onBg,
            onSurfaceVariant = onBg.copy(alpha = 0.7f),
            outline = primaryColor.copy(alpha = 0.5f),
            outlineVariant = primaryColor.copy(alpha = 0.25f)
        )
    }
}

@Composable
fun PicPickerTheme(
    themePreset: AppThemePreset = AppThemePreset.NOVEL_AI,
    customPrimaryColor: Color = Color(0xFFFF6D00),
    customBgColor: Color = Color(0xFF10111E),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreset) {
        AppThemePreset.NOVEL_AI -> NovelAiColorScheme
        AppThemePreset.MIDNIGHT_PURPLE -> MidnightPurpleColorScheme
        AppThemePreset.CLASSIC_SLATE -> ClassicSlateColorScheme
        AppThemePreset.LIGHT_MODERN -> LightModernColorScheme
        AppThemePreset.CUSTOM -> createCustomColorScheme(customPrimaryColor, customBgColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
