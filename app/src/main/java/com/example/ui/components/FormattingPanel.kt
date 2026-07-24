package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FormattingOptions
import com.example.model.SeparatorType
import com.example.model.TagPrefix
import com.example.model.TextCasing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormattingPanel(
    requestedCount: Int,
    totalAvailable: Int,
    allowDuplicates: Boolean,
    formattingOptions: FormattingOptions,
    isNaxMoe: Boolean = false,
    canDecrement: Boolean = true,
    onCountChange: (Int) -> Unit,
    onAllowDuplicatesChange: (Boolean) -> Unit,
    onFormattingOptionsChange: (FormattingOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("formatting_panel_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SELECTION & FORMATTING SETTINGS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Quantity Selector N
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Number of Pictures to Pick",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Pick $requestedCount out of ${if (totalAvailable > 0) totalAvailable else "N/A"} photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Stepper Buttons (- N +)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onCountChange(requestedCount - 1) },
                        enabled = canDecrement && requestedCount > 1,
                        modifier = Modifier.testTag("decrement_count_button")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = "$requestedCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("requested_count_text")
                    )

                    IconButton(
                        onClick = { onCountChange(requestedCount + 1) },
                        modifier = Modifier.testTag("increment_count_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Prefix Category
            Text(
                text = "Prefix",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagPrefix.values().forEach { prefixOpt ->
                    FilterChip(
                        selected = formattingOptions.tagPrefix == prefixOpt,
                        onClick = {
                            onFormattingOptionsChange(formattingOptions.copy(tagPrefix = prefixOpt))
                        },
                        label = { Text(prefixOpt.label) },
                        modifier = Modifier.testTag("prefix_chip_${prefixOpt.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Hide Extensions Toggle (.png, .jpg)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide File Extensions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isNaxMoe) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNaxMoe) "N/A for nax.moe tag database" else "e.g., 'dairi.png' → 'dairi'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = if (isNaxMoe) 0.38f else 1f)
                    )
                }
                Switch(
                    checked = formattingOptions.removeExtensions,
                    enabled = !isNaxMoe,
                    onCheckedChange = {
                        onFormattingOptionsChange(formattingOptions.copy(removeExtensions = it))
                    },
                    modifier = Modifier.testTag("hide_extensions_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Replace Underscores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clean Separators (_)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isNaxMoe) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNaxMoe) "N/A for nax.moe tag database" else "Replace '_' with spaces",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = if (isNaxMoe) 0.38f else 1f)
                    )
                }
                Switch(
                    checked = formattingOptions.replaceSeparatorsWithSpaces,
                    enabled = !isNaxMoe,
                    onCheckedChange = {
                        onFormattingOptionsChange(formattingOptions.copy(replaceSeparatorsWithSpaces = it))
                    },
                    modifier = Modifier.testTag("replace_separators_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Separators Choice
            Text(
                text = "Separator Between Names",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeparatorType.values().forEach { sep ->
                    FilterChip(
                        selected = formattingOptions.separatorType == sep,
                        onClick = {
                            onFormattingOptionsChange(formattingOptions.copy(separatorType = sep))
                        },
                        label = { Text(sep.label) },
                        modifier = Modifier.testTag("separator_chip_${sep.name}")
                    )
                }
            }

            AnimatedVisibility(visible = formattingOptions.separatorType == SeparatorType.CUSTOM) {
                OutlinedTextField(
                    value = formattingOptions.customSeparator,
                    onValueChange = {
                        onFormattingOptionsChange(formattingOptions.copy(customSeparator = it))
                    },
                    label = { Text("Custom Separator") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("custom_separator_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Casing Selector
            Text(
                text = "Text Case Format",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isNaxMoe) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextCasing.values().forEach { case ->
                    FilterChip(
                        selected = formattingOptions.casing == case,
                        enabled = !isNaxMoe,
                        onClick = {
                            onFormattingOptionsChange(formattingOptions.copy(casing = case))
                        },
                        label = { Text(case.label) },
                        modifier = Modifier.testTag("case_chip_${case.name}")
                    )
                }
            }
        }
    }
}

