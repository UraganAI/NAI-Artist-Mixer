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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MidnightPrimary
import com.example.ui.theme.NovelAiBg
import com.example.ui.theme.NovelAiPrimary
import com.example.ui.theme.SlateBg
import com.example.ui.theme.SlatePrimary

private val PopularCustomAccents = listOf(
    Color(0xFFFF6D00), // Sunset Orange
    Color(0xFFFF1744), // Crimson Red
    Color(0xFFFF4081), // Hot Pink
    Color(0xFFD500F9), // Neon Purple
    Color(0xFF651FFF), // Electric Indigo
    Color(0xFF2979FF), // Vivid Blue
    Color(0xFF00E5FF), // Cyber Cyan
    Color(0xFF00E676), // Emerald Green
    Color(0xFFFFD600)  // Golden Amber
)

@Composable
fun ThemeSelectorDialog(
    currentTheme: AppThemePreset,
    customPrimaryArgb: Long,
    customBgArgb: Long,
    onSelectTheme: (AppThemePreset) -> Unit,
    onSetCustomColors: (primaryArgb: Long, bgArgb: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCustomPrimaryHex by remember {
        mutableStateOf(String.format("%06X", (customPrimaryArgb and 0xFFFFFFL)))
    }
    var selectedCustomBgHex by remember {
        mutableStateOf(String.format("%06X", (customBgArgb and 0xFFFFFFL)))
    }
    var isEditingCustom by remember { mutableStateOf(currentTheme == AppThemePreset.CUSTOM) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                            text = "COLOR THEME",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your preferred visual style or customize your own colors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppThemePreset.entries.forEach { preset ->
                        val isSelected = preset == currentTheme

                        val (bgColor, primaryColor) = when (preset) {
                            AppThemePreset.NOVEL_AI -> Pair(NovelAiBg, NovelAiPrimary)
                            AppThemePreset.MIDNIGHT_PURPLE -> Pair(MidnightBg, MidnightPrimary)
                            AppThemePreset.CLASSIC_SLATE -> Pair(SlateBg, SlatePrimary)
                            AppThemePreset.LIGHT_MODERN -> Pair(Color(0xFFF6F7FA), Color(0xFF534BAE))
                            AppThemePreset.CUSTOM -> Pair(
                                Color(customBgArgb),
                                Color(customPrimaryArgb)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (preset == AppThemePreset.CUSTOM) {
                                        isEditingCustom = true
                                        onSelectTheme(AppThemePreset.CUSTOM)
                                    } else {
                                        isEditingCustom = false
                                        onSelectTheme(preset)
                                        onDismiss()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(bgColor)
                                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(primaryColor)
                                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Text(
                                        text = preset.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Custom Color Palette & Customizer Controls when CUSTOM is active or being edited
                    if (currentTheme == AppThemePreset.CUSTOM || isEditingCustom) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Custom Accent Color",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(PopularCustomAccents) { accentColor ->
                                        val argb = (0xFF000000L or (accentColor.toArgb().toLong() and 0xFFFFFFL))
                                        val isCurrent = (customPrimaryArgb and 0xFFFFFFL) == (argb and 0xFFFFFFL)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                                .border(
                                                    width = if (isCurrent) 3.dp else 1.dp,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    selectedCustomPrimaryHex = String.format("%06X", (argb and 0xFFFFFFL))
                                                    onSetCustomColors(argb, customBgArgb)
                                                }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Background Mode",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val isDarkBg = customBgArgb == 0xFF10111EL || customBgArgb == 0xFF121212L
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isDarkBg) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isDarkBg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetCustomColors(customPrimaryArgb, 0xFF10111EL) }
                                    ) {
                                        Text(
                                            text = "Dark Canvas",
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isDarkBg) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (!isDarkBg) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                        border = BorderStroke(1.dp, if (!isDarkBg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetCustomColors(customPrimaryArgb, 0xFFF5F5F7L) }
                                    ) {
                                        Text(
                                            text = "Light Canvas",
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (!isDarkBg) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = selectedCustomPrimaryHex,
                                        onValueChange = { selectedCustomPrimaryHex = it.take(6) },
                                        label = { Text("Primary Hex") },
                                        prefix = { Text("#") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            val parsedPrimary = runCatching {
                                                0xFF000000L or selectedCustomPrimaryHex.toLong(16)
                                            }.getOrDefault(customPrimaryArgb)
                                            onSetCustomColors(parsedPrimary, customBgArgb)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
