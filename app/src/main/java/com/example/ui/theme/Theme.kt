package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils

enum class AppThemePreset(
    val id: String,
    val displayName: String,
    val isDarkTheme: Boolean,
    val bg0Hex: String,
    val bg1Hex: String,
    val bg2Hex: String,
    val bg3Hex: String,
    val textHeadingsHex: String,
    val textMainHex: String,
    val warningHex: String,
    val textHeadingsOptionsHex: List<String>,
    val textMainOptionsHex: List<String>,
    val headingsFont: String = "Eczar",
    val defaultFont: String = "Source Sans Pro"
) {
    NOVEL_AI_DARK(
        id = "novel_ai_dark",
        displayName = "NovelAI Dark",
        isDarkTheme = true,
        bg0Hex = "#0E0F21",
        bg1Hex = "#13152C",
        bg2Hex = "#191B31",
        bg3Hex = "#22253F",
        textHeadingsHex = "#F5F3C2",
        textMainHex = "#FFFFFF",
        warningHex = "#FF7878",
        textHeadingsOptionsHex = listOf("#F5F3C2", "#EC56A7", "#75CF67", "#9773FF"),
        textMainOptionsHex = listOf("#FFFFFF", "#E7FFE9", "#FFF9C8", "#A5C9FF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    NOVEL_AI_DARK_LEGACY(
        id = "novel_ai_dark_legacy",
        displayName = "NovelAI Dark (Legacy)",
        isDarkTheme = true,
        bg0Hex = "#101224",
        bg1Hex = "#1A1C2E",
        bg2Hex = "#212335",
        bg3Hex = "#2B2D3F",
        textHeadingsHex = "#F5F3C2",
        textMainHex = "#FFFFFF",
        warningHex = "#FF7878",
        textHeadingsOptionsHex = listOf("#F5F3C2", "#EC56A7", "#75CF67", "#9773FF"),
        textMainOptionsHex = listOf("#FFFFFF", "#E7FFE9", "#FFF9C8", "#A5C9FF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    NOVEL_AI_LIGHT(
        id = "novel_ai_light",
        displayName = "NovelAI Light",
        isDarkTheme = false,
        bg0Hex = "#D3D0DB",
        bg1Hex = "#E2E0E8",
        bg2Hex = "#EEECF4",
        bg3Hex = "#FAFBFF",
        textHeadingsHex = "#855611",
        textMainHex = "#464058",
        warningHex = "#FF7878",
        textHeadingsOptionsHex = listOf("#855611", "#509397", "#BB4FB0", "#138511"),
        textMainOptionsHex = listOf("#464058"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    AMBER(
        id = "amber",
        displayName = "Amber",
        isDarkTheme = true,
        bg0Hex = "#1F0100",
        bg1Hex = "#33150B",
        bg2Hex = "#3D1F15",
        bg3Hex = "#4E2A1E",
        textHeadingsHex = "#FFCE70",
        textMainHex = "#F7F7F7",
        warningHex = "#FF5631",
        textHeadingsOptionsHex = listOf("#FFCE70", "#FF5B5B", "#D3FF9A", "#7FBCC0"),
        textMainOptionsHex = listOf("#F7F7F7"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    BUBBLEGUM(
        id = "bubblegum",
        displayName = "Bubblegum",
        isDarkTheme = false,
        bg0Hex = "#DCA7D7",
        bg1Hex = "#F5C4F1",
        bg2Hex = "#F6D3F3",
        bg3Hex = "#FFE0FC",
        textHeadingsHex = "#5D69DE",
        textMainHex = "#511466",
        warningHex = "#EB0F78",
        textHeadingsOptionsHex = listOf("#5D69DE", "#C21A57", "#2EB099", "#D356FF"),
        textMainOptionsHex = listOf("#511466"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    COUNTER_MILITANT(
        id = "counter_militant",
        displayName = "Counter Militant",
        isDarkTheme = true,
        bg0Hex = "#21312A",
        bg1Hex = "#2B3B34",
        bg2Hex = "#2F3F38",
        bg3Hex = "#30463D",
        textHeadingsHex = "#B49E69",
        textMainHex = "#C3E5C2",
        warningHex = "#BD3D3D",
        textHeadingsOptionsHex = listOf("#B49E69", "#69B0B4", "#BBB052", "#9F8F9F"),
        textMainOptionsHex = listOf("#C3E5C2"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    FROG(
        id = "frog",
        displayName = "Frog",
        isDarkTheme = false,
        bg0Hex = "#A4C37E",
        bg1Hex = "#B4D986",
        bg2Hex = "#F6EEAF",
        bg3Hex = "#E8E0A5",
        textHeadingsHex = "#633376",
        textMainHex = "#100F20",
        warningHex = "#E82929",
        textHeadingsOptionsHex = listOf("#633376", "#78849A", "#00B111", "#B23E3E"),
        textMainOptionsHex = listOf("#100F20"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    GRUVBOX_DARK(
        id = "gruvbox_dark",
        displayName = "Gruvbox Dark",
        isDarkTheme = true,
        bg0Hex = "#1C1C1C",
        bg1Hex = "#232323",
        bg2Hex = "#282828",
        bg3Hex = "#3C3836",
        textHeadingsHex = "#B8BA37",
        textMainHex = "#EBDAB2",
        warningHex = "#D61F1F",
        textHeadingsOptionsHex = listOf("#B8BA37", "#F84B3C", "#C264D1", "#00BF63"),
        textMainOptionsHex = listOf("#EBDAB2"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    INK(
        id = "ink",
        displayName = "Ink",
        isDarkTheme = false,
        bg0Hex = "#E5E1D3",
        bg1Hex = "#F0EDE3",
        bg2Hex = "#F5F2E8",
        bg3Hex = "#FBF8EE",
        textHeadingsHex = "#0A1465",
        textMainHex = "#30304B",
        warningHex = "#FF7354",
        textHeadingsOptionsHex = listOf("#0A1465", "#680971", "#650B0B", "#22691C"),
        textMainOptionsHex = listOf("#30304B"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    MATRIX(
        id = "matrix",
        displayName = "Matrix",
        isDarkTheme = true,
        bg0Hex = "#000000",
        bg1Hex = "#090909",
        bg2Hex = "#090909",
        bg3Hex = "#141714",
        textHeadingsHex = "#5EFF50",
        textMainHex = "#FFFFFF",
        warningHex = "#FF134C",
        textHeadingsOptionsHex = listOf("#5EFF50", "#FF9900", "#FF5050", "#FFF850"),
        textMainOptionsHex = listOf("#FFFFFF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    MIDNIGHT_DOLL(
        id = "midnight_doll",
        displayName = "Midnight Doll",
        isDarkTheme = true,
        bg0Hex = "#000000",
        bg1Hex = "#010101",
        bg2Hex = "#020202",
        bg3Hex = "#131313",
        textHeadingsHex = "#FF51C5",
        textMainHex = "#FAFAFA",
        warningHex = "#FF00A4",
        textHeadingsOptionsHex = listOf("#FF30BE", "#EC56A7", "#FFFFFF", "#9773FF"),
        textMainOptionsHex = listOf("#FFFFFF", "#E7FFE9", "#E1C8FF", "#FFCEF1"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    MONKEY(
        id = "monkey",
        displayName = "Monkey",
        isDarkTheme = false,
        bg0Hex = "#007FDA",
        bg1Hex = "#0093EE",
        bg2Hex = "#009DF8",
        bg3Hex = "#20ADFF",
        textHeadingsHex = "#FFFB32",
        textMainHex = "#FFFFFF",
        warningHex = "#A00000",
        textHeadingsOptionsHex = listOf("#FFFB32", "#57F287", "#180F7E", "#ACF5FF"),
        textMainOptionsHex = listOf("#FFFFFF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    PURPLE_NOIR(
        id = "purple_noir",
        displayName = "Purple Noir",
        isDarkTheme = true,
        bg0Hex = "#24172E",
        bg1Hex = "#271A31",
        bg2Hex = "#2C1F36",
        bg3Hex = "#30223B",
        textHeadingsHex = "#CFDC34",
        textMainHex = "#FFFFFF",
        warningHex = "#FF9878",
        textHeadingsOptionsHex = listOf("#CFDC34", "#9EFFA2", "#BF6CBC", "#7EBDC6"),
        textMainOptionsHex = listOf("#FFFFFF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    SAGIRI(
        id = "sagiri",
        displayName = "Sagiri",
        isDarkTheme = false,
        bg0Hex = "#E7C7D5",
        bg1Hex = "#F1DBE5",
        bg2Hex = "#F9F2F5",
        bg3Hex = "#F2EBF1",
        textHeadingsHex = "#6B916C",
        textMainHex = "#CD5582",
        warningHex = "#FF005C",
        textHeadingsOptionsHex = listOf("#6B916C", "#855611", "#A099CA", "#8C0000"),
        textMainOptionsHex = listOf("#CD5582"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    SAND(
        id = "sand",
        displayName = "Sand",
        isDarkTheme = false,
        bg0Hex = "#DFCDA4",
        bg1Hex = "#EBDAB4",
        bg2Hex = "#FBF0C9",
        bg3Hex = "#FFF4CB",
        textHeadingsHex = "#AB0613",
        textMainHex = "#3C3836",
        warningHex = "#FF0000",
        textHeadingsOptionsHex = listOf("#AB0613", "#10838A", "#876C0E", "#386831"),
        textMainOptionsHex = listOf("#3C3836"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    SLATE(
        id = "slate",
        displayName = "Slate",
        isDarkTheme = true,
        bg0Hex = "#000000",
        bg1Hex = "#02030B",
        bg2Hex = "#02030B",
        bg3Hex = "#1E2231",
        textHeadingsHex = "#71FFC3",
        textMainHex = "#9B9EB8",
        warningHex = "#FF3838",
        textHeadingsOptionsHex = listOf("#F5F3C2", "#EC56A7", "#75CF67", "#9773FF"),
        textMainOptionsHex = listOf("#FFFFFF", "#E7FFE9", "#FFF9C8", "#A5C9FF"),
        headingsFont = "Kanit",
        defaultFont = "Atkinson Hyperlegible"
    ),
    SUBTLE_TERMINAL(
        id = "subtle_terminal",
        displayName = "Subtle Terminal",
        isDarkTheme = true,
        bg0Hex = "#1E2129",
        bg1Hex = "#252931",
        bg2Hex = "#282C34",
        bg3Hex = "#333842",
        textHeadingsHex = "#57B260",
        textMainHex = "#F7F7F7",
        warningHex = "#FFA5A5",
        textHeadingsOptionsHex = listOf("#57B260", "#5199AF", "#C56565", "#8B6BBF"),
        textMainOptionsHex = listOf("#F7F7F7"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    VIBROWAVE(
        id = "vibrowave",
        displayName = "Vibrowave",
        isDarkTheme = true,
        bg0Hex = "#00001E",
        bg1Hex = "#09032C",
        bg2Hex = "#0D0637",
        bg3Hex = "#191145",
        textHeadingsHex = "#CC308D",
        textMainHex = "#FFFDD2",
        warningHex = "#FF5C46",
        textHeadingsOptionsHex = listOf("#CC308D", "#30CCCC", "#E4BA25", "#FF0000"),
        textMainOptionsHex = listOf("#FFFDD2"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    WINE(
        id = "wine",
        displayName = "Wine",
        isDarkTheme = true,
        bg0Hex = "#2E1622",
        bg1Hex = "#351D29",
        bg2Hex = "#38202C",
        bg3Hex = "#3E2230",
        textHeadingsHex = "#ABE471",
        textMainHex = "#C6D1AE",
        warningHex = "#D31E5F",
        textHeadingsOptionsHex = listOf("#ABE471", "#E4DF71", "#B74848", "#96C3C6"),
        textMainOptionsHex = listOf("#C6D1AE"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom Theme",
        isDarkTheme = true,
        bg0Hex = "#0E0F21",
        bg1Hex = "#13152C",
        bg2Hex = "#191B31",
        bg3Hex = "#22253F",
        textHeadingsHex = "#F5F3C2",
        textMainHex = "#FFFFFF",
        warningHex = "#FF7878",
        textHeadingsOptionsHex = listOf("#F5F3C2", "#EC56A7", "#75CF67", "#9773FF"),
        textMainOptionsHex = listOf("#FFFFFF", "#E7FFE9", "#FFF9C8", "#A5C9FF"),
        headingsFont = "Eczar",
        defaultFont = "Source Sans Pro"
    );

    fun parseColor(hex: String): Color {
        val clean = hex.removePrefix("#")
        val argb = if (clean.length == 6) {
            0xFF000000L or clean.toLong(16)
        } else if (clean.length == 8) {
            clean.toLong(16)
        } else {
            0xFF000000L
        }
        return Color(argb)
    }

    val bg0: Color get() = parseColor(bg0Hex)
    val bg1: Color get() = parseColor(bg1Hex)
    val bg2: Color get() = parseColor(bg2Hex)
    val bg3: Color get() = parseColor(bg3Hex)
    val textHeadings: Color get() = parseColor(textHeadingsHex)
    val textMain: Color get() = parseColor(textMainHex)
    val textHeadingsOptions: List<Color> get() = textHeadingsOptionsHex.map { parseColor(it) }
    val textMainOptions: List<Color> get() = textMainOptionsHex.map { parseColor(it) }

    companion object {
        fun fromId(id: String?): AppThemePreset {
            if (id == null) return NOVEL_AI_DARK
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: NOVEL_AI_DARK
        }
    }
}

fun createThemeColorScheme(
    headerColor: Color,
    paragraphColor: Color,
    foregroundColor: Color,
    backgroundColor: Color,
    darkBgColor: Color,
    inputBgColor: Color
): ColorScheme {
    val isLight = ColorUtils.calculateLuminance(backgroundColor.toArgb()) > 0.45
    val onPrimaryColor = if (ColorUtils.calculateLuminance(headerColor.toArgb()) > 0.5) Color(0xFF10111E) else Color.White

    return if (isLight) {
        lightColorScheme(
            primary = headerColor,
            onPrimary = onPrimaryColor,
            primaryContainer = darkBgColor,
            onPrimaryContainer = headerColor,
            secondary = headerColor,
            secondaryContainer = darkBgColor,
            onSecondaryContainer = paragraphColor,
            tertiary = headerColor,
            onTertiary = onPrimaryColor,
            tertiaryContainer = darkBgColor,
            onTertiaryContainer = paragraphColor,
            background = backgroundColor,
            onBackground = paragraphColor,
            surface = backgroundColor,
            onSurface = paragraphColor,
            surfaceVariant = inputBgColor,
            onSurfaceVariant = paragraphColor.copy(alpha = 0.85f),
            surfaceTint = headerColor,
            inverseSurface = paragraphColor,
            inverseOnSurface = backgroundColor,
            error = Color(0xFFFF7878),
            onError = Color.White,
            errorContainer = Color(0xFFFF7878).copy(alpha = 0.2f),
            onErrorContainer = Color(0xFFFF7878),
            outline = foregroundColor,
            outlineVariant = foregroundColor.copy(alpha = 0.7f),
            scrim = Color.Black.copy(alpha = 0.6f),
            surfaceContainerLowest = inputBgColor,
            surfaceContainerLow = darkBgColor,
            surfaceContainer = darkBgColor,
            surfaceContainerHigh = inputBgColor,
            surfaceContainerHighest = inputBgColor
        )
    } else {
        darkColorScheme(
            primary = headerColor,
            onPrimary = onPrimaryColor,
            primaryContainer = darkBgColor,
            onPrimaryContainer = headerColor,
            secondary = headerColor,
            secondaryContainer = darkBgColor,
            onSecondaryContainer = paragraphColor,
            tertiary = headerColor,
            onTertiary = onPrimaryColor,
            tertiaryContainer = darkBgColor,
            onTertiaryContainer = paragraphColor,
            background = backgroundColor,
            onBackground = paragraphColor,
            surface = backgroundColor,
            onSurface = paragraphColor,
            surfaceVariant = inputBgColor,
            onSurfaceVariant = paragraphColor.copy(alpha = 0.85f),
            surfaceTint = headerColor,
            inverseSurface = paragraphColor,
            inverseOnSurface = backgroundColor,
            error = Color(0xFFFF7878),
            onError = Color.Black,
            errorContainer = Color(0xFFFF7878).copy(alpha = 0.25f),
            onErrorContainer = Color(0xFFFF7878),
            outline = foregroundColor,
            outlineVariant = foregroundColor.copy(alpha = 0.7f),
            scrim = Color.Black.copy(alpha = 0.6f),
            surfaceContainerLowest = inputBgColor,
            surfaceContainerLow = darkBgColor,
            surfaceContainer = darkBgColor,
            surfaceContainerHigh = inputBgColor,
            surfaceContainerHighest = inputBgColor
        )
    }
}

fun createThemeTypography(headingsFont: String, defaultFont: String): Typography {
    val headingFontFamily = when (headingsFont.lowercase()) {
        "eczar", "serif" -> FontFamily.Serif
        "kanit", "sans-serif", "sansserif", "source sans pro", "atkinson hyperlegible", "roboto" -> FontFamily.SansSerif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Serif
    }

    val bodyFontFamily = when (defaultFont.lowercase()) {
        "eczar", "serif" -> FontFamily.Serif
        "kanit", "sans-serif", "sansserif", "source sans pro", "atkinson hyperlegible", "roboto", "open sans" -> FontFamily.SansSerif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }

    return Typography(
        titleLarge = TextStyle(fontFamily = headingFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp),
        titleSmall = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        headlineLarge = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        headlineMedium = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        headlineSmall = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
        bodyLarge = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelLarge = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = bodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp)
    )
}

@Composable
fun PicPickerTheme(
    themePreset: AppThemePreset = AppThemePreset.NOVEL_AI_DARK,
    headerColor: Color = themePreset.textHeadings,
    paragraphColor: Color = themePreset.textMain,
    foregroundColor: Color = themePreset.bg3,
    backgroundColor: Color = themePreset.bg2,
    darkBgColor: Color = themePreset.bg1,
    inputBgColor: Color = themePreset.bg0,
    headingsFont: String = themePreset.headingsFont,
    defaultFont: String = themePreset.defaultFont,
    content: @Composable () -> Unit
) {
    val activeHeader = if (themePreset == AppThemePreset.CUSTOM) headerColor else themePreset.textHeadings
    val activeParagraph = if (themePreset == AppThemePreset.CUSTOM) paragraphColor else themePreset.textMain
    val activeForeground = if (themePreset == AppThemePreset.CUSTOM) foregroundColor else themePreset.bg3
    val activeBackground = if (themePreset == AppThemePreset.CUSTOM) backgroundColor else themePreset.bg2
    val activeDarkBg = if (themePreset == AppThemePreset.CUSTOM) darkBgColor else themePreset.bg1
    val activeInputBg = if (themePreset == AppThemePreset.CUSTOM) inputBgColor else themePreset.bg0
    val activeHeadingsFont = if (themePreset == AppThemePreset.CUSTOM) headingsFont else themePreset.headingsFont
    val activeDefaultFont = if (themePreset == AppThemePreset.CUSTOM) defaultFont else themePreset.defaultFont

    val colorScheme = createThemeColorScheme(
        headerColor = activeHeader,
        paragraphColor = activeParagraph,
        foregroundColor = activeForeground,
        backgroundColor = activeBackground,
        darkBgColor = activeDarkBg,
        inputBgColor = activeInputBg
    )

    val typography = createThemeTypography(
        headingsFont = activeHeadingsFont,
        defaultFont = activeDefaultFont
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
