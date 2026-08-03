package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TagManagementDialog(
    title: String,
    allTags: Set<String>,
    initialSelectedTags: Set<String> = emptySet(),
    itemCounts: Map<String, Int> = emptyMap(),
    confirmButtonText: String = "Apply",
    itemTypeName: String = "artist mixes",
    onCreateTag: (String) -> Unit,
    onDeleteTag: ((String) -> Unit)? = null,
    onRenameTag: ((String, String) -> Unit)? = null,
    onSelectFilterTag: ((String) -> Unit)? = null,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTags by remember(initialSelectedTags) { mutableStateOf(initialSelectedTags.toMutableSet()) }
    var newTagInput by remember { mutableStateOf("") }

    var tagToRename by remember { mutableStateOf<String?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Create new tag row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        placeholder = { Text("New tag name...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tag_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val isAddEnabled = newTagInput.trim().isNotEmpty()
                    Button(
                        onClick = {
                            val trimmed = newTagInput.trim()
                            if (trimmed.isNotBlank()) {
                                onCreateTag(trimmed)
                                selectedTags = (selectedTags + trimmed).toMutableSet()
                                newTagInput = ""
                            }
                        },
                        enabled = isAddEnabled,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_tag_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tag")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (allTags.isEmpty()) "No tags created yet." else if (onSelectFilterTag != null) "Tap a tag to filter:" else "Select tags:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tags list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (allTags.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create your first tag above!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(allTags.toList().sorted()) { tag ->
                                val isChecked = selectedTags.contains(tag)
                                val count = itemCounts[tag] ?: 0

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChecked)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (onSelectFilterTag != null) {
                                                if (count == 0) {
                                                    Toast.makeText(context, "Tag '$tag' has no $itemTypeName in it", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onSelectFilterTag(tag)
                                                    onDismiss()
                                                }
                                            } else {
                                                selectedTags = if (isChecked) {
                                                    (selectedTags - tag).toMutableSet()
                                                } else {
                                                    (selectedTags + tag).toMutableSet()
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (onSelectFilterTag == null) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        selectedTags = if (checked) {
                                                            (selectedTags + tag).toMutableSet()
                                                        } else {
                                                            (selectedTags - tag).toMutableSet()
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Label,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }

                                            Text(
                                                text = "$tag ($count)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                fontSize = 14.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (onRenameTag != null) {
                                                IconButton(
                                                    onClick = {
                                                        tagToRename = tag
                                                        renameInputText = tag
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Rename Tag",
                                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            if (onDeleteTag != null) {
                                                IconButton(
                                                    onClick = {
                                                        onDeleteTag(tag)
                                                        selectedTags = (selectedTags - tag).toMutableSet()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Tag",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    if (onSelectFilterTag == null) {
                        Button(
                            onClick = {
                                onConfirm(selectedTags)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("confirm_tags_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(confirmButtonText, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // Rename Tag Dialog
    tagToRename?.let { oldTag ->
        AlertDialog(
            onDismissRequest = { tagToRename = null },
            title = {
                Text(
                    text = "Rename Tag",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Tag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameInputText.trim()
                        if (newName.isNotBlank() && newName != oldTag) {
                            onRenameTag?.invoke(oldTag, newName)
                        }
                        tagToRename = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
