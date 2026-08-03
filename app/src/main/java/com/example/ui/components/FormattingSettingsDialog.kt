package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FormattingOptions
import com.example.util.mouseWheelScroll

@Composable
fun FormattingSettingsDialog(
    requestedCount: Int,
    totalAvailable: Int,
    allowDuplicates: Boolean,
    formattingOptions: FormattingOptions,
    isNaxMoe: Boolean = false,
    canDecrement: Boolean = true,
    onCountChange: (Int) -> Unit,
    onAllowDuplicatesChange: (Boolean) -> Unit,
    onFormattingOptionsChange: (FormattingOptions) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(vertical = 16.dp)
                    .testTag("formatting_settings_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                        .mouseWheelScroll(scrollState)
                ) {
                    FormattingPanel(
                        requestedCount = requestedCount,
                        totalAvailable = totalAvailable,
                        allowDuplicates = allowDuplicates,
                        formattingOptions = formattingOptions,
                        isNaxMoe = isNaxMoe,
                        canDecrement = canDecrement,
                        onCountChange = onCountChange,
                        onAllowDuplicatesChange = onAllowDuplicatesChange,
                        onFormattingOptionsChange = onFormattingOptionsChange
                    )
                }
            }
        }
    }
}
