package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppThemePreset

enum class ThemeColorTarget(val label: String) {
    HEADER("Header Color"),
    PARAGRAPH("Other Text Color"),
    INPUT_BACKGROUND("Input Background"),
    DARK_BACKGROUND("Dark Background"),
    BACKGROUND("Background"),
    FOREGROUND("Foreground")
}

fun getFontFamilyByName(fontName: String): FontFamily {
    return when (fontName.lowercase()) {
        "eczar", "serif" -> FontFamily.Serif
        "kanit", "sans-serif", "sansserif", "source sans pro", "atkinson hyperlegible", "roboto", "open sans" -> FontFamily.SansSerif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}

@Composable
fun ThemeSelectorDialog(
    currentTheme: AppThemePreset,
    headerColorArgb: Long,
    paragraphColorArgb: Long,
    foregroundColorArgb: Long,
    backgroundColorArgb: Long,
    darkBgColorArgb: Long,
    inputBgColorArgb: Long,
    headingsFont: String = "Eczar",
    defaultFont: String = "Source Sans Pro",
    onSelectPreset: (AppThemePreset) -> Unit,
    onUpdateThemeColor: (
        header: Long,
        paragraph: Long,
        foreground: Long,
        background: Long,
        darkBg: Long,
        inputBg: Long
    ) -> Unit,
    onUpdateThemeFonts: (headings: String, default: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var activePickerTarget by remember { mutableStateOf<ThemeColorTarget?>(null) }

    val activeHeader = Color(headerColorArgb)
    val activeParagraph = Color(paragraphColorArgb)
    val activeForeground = Color(foregroundColorArgb)
    val activeBackground = Color(backgroundColorArgb)
    val activeDarkBg = Color(darkBgColorArgb)
    val activeInputBg = Color(inputBgColorArgb)

    val fontChoicesHeader = listOf("Eczar", "Kanit", "Source Sans Pro", "Serif", "Sans-Serif", "Monospace", "Cursive")
    val fontChoicesParagraph = listOf("Source Sans Pro", "Atkinson Hyperlegible", "Eczar", "Kanit", "Serif", "Sans-Serif", "Monospace")

    val scrollState = rememberScrollState()

    // Smooth auto-scroll when opening Custom Theme
    LaunchedEffect(currentTheme) {
        if (currentTheme == AppThemePreset.CUSTOM) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Theme ordering: NovelAI themes first, then Alphabetical presets, then Custom Theme last.
    val orderedPresets = remember {
        val presets = AppThemePreset.entries.toList()
        val novelAi = presets.filter { it.name.startsWith("NOVEL_AI") }
        val custom = presets.filter { it == AppThemePreset.CUSTOM }
        val others = presets.filter { !it.name.startsWith("NOVEL_AI") && it != AppThemePreset.CUSTOM }
            .sortedBy { it.displayName }
        novelAi + others + custom
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "THEME SELECTOR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List of Themes and Custom Settings
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PRESET THEMES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    orderedPresets.forEach { preset ->
                        val isSelected = preset == currentTheme

                        // Theme item square:
                        // Background = theme's own background color (bg2)
                        // Text = theme's accent color (textHeadings)
                        // Border = foreground color (bg3) or primary if selected
                        Surface(
                            onClick = { onSelectPreset(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = preset.bg2,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    preset.bg3
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else preset.bg3,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = preset.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            fontFamily = getFontFamilyByName(preset.headingsFont)
                                        ),
                                        color = preset.textHeadings
                                    )
                                }

                                // Color Swatches Preview
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(preset.textHeadings)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(preset.textMain)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(preset.bg0)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(preset.bg1)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(preset.bg3)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // Custom Theme Controls Section
                    if (currentTheme == AppThemePreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "CUSTOM THEME SETTINGS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Font selection
                        FontSelectorDropdown(
                            label = "Header Font",
                            selectedFont = headingsFont,
                            options = fontChoicesHeader,
                            onSelectFont = { font ->
                                onUpdateThemeFonts(font, defaultFont)
                            }
                        )

                        FontSelectorDropdown(
                            label = "Other Text Font",
                            selectedFont = defaultFont,
                            options = fontChoicesParagraph,
                            onSelectFont = { font ->
                                onUpdateThemeFonts(headingsFont, font)
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Color Targets (6 colors: Header, Paragraph, Input BG, Dark BG, BG, Foreground)
                        ThemeCategoryRow(
                            title = ThemeColorTarget.HEADER.label,
                            options = currentTheme.textHeadingsOptions,
                            selectedColor = activeHeader,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    selected.toArgb().toLong(),
                                    paragraphColorArgb,
                                    foregroundColorArgb,
                                    backgroundColorArgb,
                                    darkBgColorArgb,
                                    inputBgColorArgb
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.HEADER }
                        )

                        ThemeCategoryRow(
                            title = ThemeColorTarget.PARAGRAPH.label,
                            options = currentTheme.textMainOptions,
                            selectedColor = activeParagraph,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    headerColorArgb,
                                    selected.toArgb().toLong(),
                                    foregroundColorArgb,
                                    backgroundColorArgb,
                                    darkBgColorArgb,
                                    inputBgColorArgb
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.PARAGRAPH }
                        )

                        ThemeCategoryRow(
                            title = ThemeColorTarget.INPUT_BACKGROUND.label,
                            options = listOf(currentTheme.bg0),
                            selectedColor = activeInputBg,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    headerColorArgb,
                                    paragraphColorArgb,
                                    foregroundColorArgb,
                                    backgroundColorArgb,
                                    darkBgColorArgb,
                                    selected.toArgb().toLong()
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.INPUT_BACKGROUND }
                        )

                        ThemeCategoryRow(
                            title = ThemeColorTarget.DARK_BACKGROUND.label,
                            options = listOf(currentTheme.bg1),
                            selectedColor = activeDarkBg,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    headerColorArgb,
                                    paragraphColorArgb,
                                    foregroundColorArgb,
                                    backgroundColorArgb,
                                    selected.toArgb().toLong(),
                                    inputBgColorArgb
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.DARK_BACKGROUND }
                        )

                        ThemeCategoryRow(
                            title = ThemeColorTarget.BACKGROUND.label,
                            options = listOf(currentTheme.bg2),
                            selectedColor = activeBackground,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    headerColorArgb,
                                    paragraphColorArgb,
                                    foregroundColorArgb,
                                    selected.toArgb().toLong(),
                                    darkBgColorArgb,
                                    inputBgColorArgb
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.BACKGROUND }
                        )

                        ThemeCategoryRow(
                            title = ThemeColorTarget.FOREGROUND.label,
                            options = listOf(currentTheme.bg3),
                            selectedColor = activeForeground,
                            onSelectColor = { selected ->
                                onUpdateThemeColor(
                                    headerColorArgb,
                                    paragraphColorArgb,
                                    selected.toArgb().toLong(),
                                    backgroundColorArgb,
                                    darkBgColorArgb,
                                    inputBgColorArgb
                                )
                            },
                            onOpenPicker = { activePickerTarget = ThemeColorTarget.FOREGROUND }
                        )
                    }
                }
            }
        }
    }

    // Custom Color Picker Dialog
    activePickerTarget?.let { target ->
        val initialColor = when (target) {
            ThemeColorTarget.HEADER -> activeHeader
            ThemeColorTarget.PARAGRAPH -> activeParagraph
            ThemeColorTarget.INPUT_BACKGROUND -> activeInputBg
            ThemeColorTarget.DARK_BACKGROUND -> activeDarkBg
            ThemeColorTarget.BACKGROUND -> activeBackground
            ThemeColorTarget.FOREGROUND -> activeForeground
        }

        CustomColorPickerDialog(
            title = "Pick ${target.label}",
            initialColor = initialColor,
            onApply = { newColor ->
                val newArgb = newColor.toArgb().toLong()
                when (target) {
                    ThemeColorTarget.HEADER -> onUpdateThemeColor(
                        newArgb, paragraphColorArgb, foregroundColorArgb, backgroundColorArgb, darkBgColorArgb, inputBgColorArgb
                    )
                    ThemeColorTarget.PARAGRAPH -> onUpdateThemeColor(
                        headerColorArgb, newArgb, foregroundColorArgb, backgroundColorArgb, darkBgColorArgb, inputBgColorArgb
                    )
                    ThemeColorTarget.INPUT_BACKGROUND -> onUpdateThemeColor(
                        headerColorArgb, paragraphColorArgb, foregroundColorArgb, backgroundColorArgb, darkBgColorArgb, newArgb
                    )
                    ThemeColorTarget.DARK_BACKGROUND -> onUpdateThemeColor(
                        headerColorArgb, paragraphColorArgb, foregroundColorArgb, backgroundColorArgb, newArgb, inputBgColorArgb
                    )
                    ThemeColorTarget.BACKGROUND -> onUpdateThemeColor(
                        headerColorArgb, paragraphColorArgb, foregroundColorArgb, newArgb, darkBgColorArgb, inputBgColorArgb
                    )
                    ThemeColorTarget.FOREGROUND -> onUpdateThemeColor(
                        headerColorArgb, paragraphColorArgb, newArgb, backgroundColorArgb, darkBgColorArgb, inputBgColorArgb
                    )
                }
                activePickerTarget = null
            },
            onDismiss = { activePickerTarget = null }
        )
    }
}

@Composable
private fun FontSelectorDropdown(
    label: String,
    selectedFont: String,
    options: List<String>,
    onSelectFont: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedFont,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = getFontFamilyByName(selectedFont),
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Font",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                options.forEach { fontName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = fontName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = getFontFamilyByName(fontName),
                                    fontWeight = if (fontName.equals(selectedFont, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (fontName.equals(selectedFont, ignoreCase = true))
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelectFont(fontName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCategoryRow(
    title: String,
    options: List<Color>,
    selectedColor: Color,
    onSelectColor: (Color) -> Unit,
    onOpenPicker: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preset Color Boxes
            options.forEach { color ->
                val isSelected = colorsMatch(color, selectedColor)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectColor(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val checkTint = if (ColorUtilsCalculateLuminance(color) > 0.5) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = checkTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Custom Eyedropper Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onOpenPicker() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Colorize,
                    contentDescription = "Custom Eyedropper",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun colorsMatch(c1: Color, c2: Color): Boolean {
    val argb1 = c1.toArgb() and 0xFFFFFF
    val argb2 = c2.toArgb() and 0xFFFFFF
    return argb1 == argb2
}

private fun ColorUtilsCalculateLuminance(color: Color): Double {
    return androidx.core.graphics.ColorUtils.calculateLuminance(color.toArgb())
}

@Composable
private fun CustomColorPickerDialog(
    title: String,
    initialColor: Color,
    onApply: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(String.format("%06X", (initialColor.toArgb() and 0xFFFFFF))) }
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val activeColor = Color(red, green, blue, 1f)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Color Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val textTint = if (ColorUtilsCalculateLuminance(activeColor) > 0.5) Color.Black else Color.White
                    Text(
                        text = "#${String.format("%06X", (activeColor.toArgb() and 0xFFFFFF))}",
                        fontWeight = FontWeight.Bold,
                        color = textTint
                    )
                }

                // RGB Sliders
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Red: ${(red * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = red,
                        onValueChange = {
                            red = it
                            hexInput = String.format("%06X", (Color(red, green, blue).toArgb() and 0xFFFFFF))
                        },
                        valueRange = 0f..1f
                    )

                    Text("Green: ${(green * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = green,
                        onValueChange = {
                            green = it
                            hexInput = String.format("%06X", (Color(red, green, blue).toArgb() and 0xFFFFFF))
                        },
                        valueRange = 0f..1f
                    )

                    Text("Blue: ${(blue * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = blue,
                        onValueChange = {
                            blue = it
                            hexInput = String.format("%06X", (Color(red, green, blue).toArgb() and 0xFFFFFF))
                        },
                        valueRange = 0f..1f
                    )
                }

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val clean = input.uppercase().take(6).filter { it in "0123456789ABCDEF" }
                        hexInput = clean
                        if (clean.length == 6) {
                            runCatching {
                                val parse = Color(0xFF000000L or clean.toLong(16))
                                red = parse.red
                                green = parse.green
                                blue = parse.blue
                            }
                        }
                    },
                    label = { Text("Hex Color Code") },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onApply(activeColor) }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
