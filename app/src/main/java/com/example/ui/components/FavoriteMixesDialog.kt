package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.model.FavoriteMix
import com.example.model.FormattingOptions
import com.example.util.NameFormatter
import com.example.util.mouseWheelScrollGrid

enum class SortMode(val label: String) {
    DATE_ASC("Date (Oldest First)"),
    DATE_DESC("Date (Newest First)"),
    NAME_ASC("Name (A - Z)"),
    NAME_DESC("Name (Z - A)")
}

@Composable
fun FavoriteMixesDialog(
    favoriteMixes: List<FavoriteMix>,
    formattingOptions: FormattingOptions = FormattingOptions(),
    viewModel: com.example.viewmodel.MainViewModel? = null,
    onSelectMix: (FavoriteMix) -> Unit,
    onDeleteMix: (String) -> Unit,
    onRenameMix: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    var editingMix by remember { mutableStateOf<FavoriteMix?>(null) }
    var mixToDelete by remember { mutableStateOf<FavoriteMix?>(null) }
    var editingTitleText by remember { mutableStateOf("") }
    var editingTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var previewMixIndex by remember { mutableIntStateOf(-1) }

    var sortMode by remember { mutableStateOf(SortMode.DATE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTag by remember { mutableStateOf<String?>(null) }
    var showTagFilterDialog by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    val filteredAndSortedMixes = remember(favoriteMixes, searchQuery, sortMode, selectedFilterTag) {
        val filtered = favoriteMixes.filter { mix ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                mix.title.lowercase().contains(q) || mix.pictures.any { pic -> pic.fileName.lowercase().contains(q) }
            }
            val matchesTag = if (selectedFilterTag == null) true else mix.tags.contains(selectedFilterTag)
            matchesSearch && matchesTag
        }

        when (sortMode) {
            SortMode.DATE_ASC -> filtered
            SortMode.DATE_DESC -> filtered.reversed()
            SortMode.NAME_ASC -> filtered.sortedBy { it.title.ifBlank { "Mix" }.lowercase() }
            SortMode.NAME_DESC -> filtered.sortedByDescending { it.title.ifBlank { "Mix" }.lowercase() }
        }
    }

    val handleDismiss = {
        selectedFilterTag = null
        onDismiss()
    }

    Dialog(
        onDismissRequest = handleDismiss,
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
                    onClick = handleDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume clicks inside dialog
                    )
                    .testTag("favorite_mixes_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MultipleHeartsIcon(
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Favorite Mixes",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${favoriteMixes.size} saved combinations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Search Toggle Button
                            IconButton(
                                onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) searchQuery = ""
                                },
                                modifier = Modifier.testTag("search_favorite_mixes_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Mixes",
                                    tint = if (isSearchActive || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Sort Button
                            Box {
                                IconButton(
                                    onClick = { showSortMenu = true },
                                    modifier = Modifier.testTag("sort_favorite_mixes_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort Mixes",
                                        tint = if (sortMode != SortMode.DATE_ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.label) },
                                            onClick = {
                                                sortMode = mode
                                                showSortMenu = false
                                            },
                                            leadingIcon = if (sortMode == mode) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            // Tag Filter Button
                            IconButton(
                                onClick = {
                                    if (selectedFilterTag != null) {
                                        selectedFilterTag = null
                                    } else {
                                        showTagFilterDialog = true
                                    }
                                },
                                modifier = Modifier.testTag("tag_filter_favorite_mixes_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = "Filter by Tag",
                                    tint = if (selectedFilterTag != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_favorite_mixes_dialog")
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                            }
                        }
                    }

                    if (isSearchActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search mix name or artist...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        isSearchActive = false
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search and close")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                                .testTag("search_favorite_mixes_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (favoriteMixes.isEmpty()) {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        modifier = Modifier.padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MultipleHeartsIcon(
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Favorite Mixes Saved Yet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "In the 'Try Mix' window, generate or customize your artist mix and tap 'Save Mix' to store your favorite combinations here!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (filteredAndSortedMixes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No mixes match '$searchQuery'",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Mixes Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            state = gridState,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .mouseWheelScrollGrid(gridState)
                        ) {
                            itemsIndexed(filteredAndSortedMixes, key = { _, item -> item.id }) { index, mix ->
                                val defaultMixNumber = favoriteMixes.indexOfFirst { it.id == mix.id } + 1
                                FavoriteMixCard(
                                    mix = mix,
                                    mixNumber = defaultMixNumber,
                                    formattingOptions = formattingOptions,
                                    onSelect = {
                                        onSelectMix(mix)
                                        onDismiss()
                                    },
                                    onClickImage = {
                                        previewMixIndex = favoriteMixes.indexOfFirst { it.id == mix.id }
                                    },
                                    onRename = {
                                        editingMix = mix
                                        editingTitleText = mix.title.ifBlank { "Mix#$defaultMixNumber" }
                                        editingTags = mix.tags.toSet()
                                    },
                                    onDelete = { mixToDelete = mix }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    mixToDelete?.let { mix ->
        val defaultMixNumber = favoriteMixes.indexOfFirst { it.id == mix.id } + 1
        val mixName = mix.title.ifBlank { "Mix#$defaultMixNumber" }
        AlertDialog(
            onDismissRequest = { mixToDelete = null },
            title = {
                Text(
                    text = "Delete Favorite Mix",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '$mixName'?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMix(mix.id)
                        mixToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mixToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename & Tag Mix Dialog
    editingMix?.let { mix ->
        val mixTags = viewModel?.mixTags?.collectAsStateWithLifecycle()?.value ?: emptySet()
        var newTagInputText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { editingMix = null },
            title = {
                Text(
                    text = "Edit Mix Name & Tags",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingTitleText,
                        onValueChange = { editingTitleText = it },
                        label = { Text("Mix Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_mix_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tags for this mix:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Create custom tag row inside rename dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTagInputText,
                            onValueChange = { newTagInputText = it },
                            placeholder = { Text("Add new tag...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val clean = newTagInputText.trim()
                                if (clean.isNotBlank()) {
                                    viewModel?.createMixTag(clean)
                                    editingTags = editingTags + clean
                                    newTagInputText = ""
                                }
                            },
                            enabled = newTagInputText.trim().isNotEmpty(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (mixTags.isEmpty() && editingTags.isEmpty()) {
                        Text(
                            text = "No tags created yet. Type a tag above to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        val allAvailableTags = (mixTags + editingTags).toList().sorted()
                        LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                            items(allAvailableTags) { tag ->
                                val isChecked = editingTags.contains(tag)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            editingTags = if (isChecked) editingTags - tag else editingTags + tag
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            editingTags = if (checked) editingTags + tag else editingTags - tag
                                        }
                                    )
                                    Text(text = tag, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingTitleText.isNotBlank()) {
                            onRenameMix(mix.id, editingTitleText.trim())
                        }
                        viewModel?.updateFavoriteMixTags(mix.id, editingTags.toList())
                        editingMix = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMix = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTagFilterDialog) {
        val mixTags = viewModel?.mixTags?.collectAsStateWithLifecycle()?.value ?: emptySet()
        val mixItemCounts = remember(mixTags, favoriteMixes) {
            mixTags.associateWith { tag ->
                favoriteMixes.count { it.tags.contains(tag) }
            }
        }

        TagManagementDialog(
            title = "Filter Mixes by Tag",
            allTags = mixTags,
            initialSelectedTags = if (selectedFilterTag != null) setOf(selectedFilterTag!!) else emptySet(),
            itemCounts = mixItemCounts,
            confirmButtonText = "Apply Tag Filter",
            itemTypeName = "artist mixes",
            onCreateTag = { viewModel?.createMixTag(it) },
            onDeleteTag = { viewModel?.deleteMixTag(it) },
            onRenameTag = { oldTag, newTag -> viewModel?.renameMixTag(oldTag, newTag) },
            onSelectFilterTag = { selectedTag ->
                selectedFilterTag = selectedTag
            },
            onConfirm = { selectedTags ->
                showTagFilterDialog = false
                selectedFilterTag = selectedTags.firstOrNull()
            },
            onDismiss = { showTagFilterDialog = false }
        )
    }

    // Lightbox / Fullscreen Preview Dialog
    if (previewMixIndex in favoriteMixes.indices) {
        FavoriteMixLightboxDialog(
            favoriteMixes = favoriteMixes,
            initialIndex = previewMixIndex,
            onSelectMix = { mix ->
                onSelectMix(mix)
                onDismiss()
            },
            onDeleteMix = { mixId ->
                onDeleteMix(mixId)
            },
            onDismiss = { previewMixIndex = -1 }
        )
    }
}

@Composable
private fun FavoriteMixCard(
    mix: FavoriteMix,
    mixNumber: Int,
    formattingOptions: FormattingOptions,
    onSelect: () -> Unit,
    onClickImage: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val displayTitle = mix.title.ifBlank { "Mix#$mixNumber" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("favorite_mix_card_${mix.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview Image - Clicking it opens full view
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable { onClickImage() }
                    .testTag("favorite_mix_image_${mix.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (!mix.imagePath.isNullByBlank() && File(mix.imagePath).exists()) {
                    SubcomposeAsyncImage(
                        model = File(mix.imagePath),
                        contentDescription = "Mix Preview",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${mix.pictures.size} Artists",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details - Title is clickable to rename
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onRename() }
                        .padding(vertical = 2.dp)
                        .testTag("rename_mix_button_${mix.id}")
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Mix",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${mix.pictures.size} Artists: " + mix.pictures.joinToString(", ") { it.fileName.substringBeforeLast('.') },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mix.seed != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Seed: ${mix.seed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (mix.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tags: ${mix.tags.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = {
                        val formattedContext = NameFormatter.formatPictureNames(mix.pictures, formattingOptions)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Mix Tags", formattedContext)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied mix tags to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("copy_mix_context_button_${mix.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Mix Tags",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onSelect,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("load_mix_button_${mix.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Load Mix",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_mix_button_${mix.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Mix",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteMixLightboxDialog(
    favoriteMixes: List<FavoriteMix>,
    initialIndex: Int,
    onSelectMix: (FavoriteMix) -> Unit,
    onDeleteMix: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (favoriteMixes.isEmpty() || initialIndex !in favoriteMixes.indices) {
        onDismiss()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(favoriteMixes.indices),
        pageCount = { favoriteMixes.size }
    )

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.94f),
            shape = RoundedCornerShape(24.dp)
        ) {
            val currentPageIndex = pagerState.currentPage.coerceIn(favoriteMixes.indices)
            val currentMix = favoriteMixes[currentPageIndex]
            val defaultMixNumber = favoriteMixes.size - currentPageIndex
            val displayTitle = currentMix.title.ifBlank { "Mix#$defaultMixNumber" }

            Box(modifier = Modifier.fillMaxSize()) {
                // Horizontal Pager for swiping between favorite mixes
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val mix = favoriteMixes[page]
                    val mixNumber = favoriteMixes.size - page
                    val title = mix.title.ifBlank { "Mix#$mixNumber" }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 70.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!mix.imagePath.isNullByBlank() && File(mix.imagePath).exists()) {
                            ZoomableBox(modifier = Modifier.fillMaxSize()) {
                                SubcomposeAsyncImage(
                                    model = File(mix.imagePath),
                                    contentDescription = title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${mix.pictures.size} Artists: " + mix.pictures.joinToString(", ") { it.fileName.substringBeforeLast('.') },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${pagerState.currentPage + 1}/${favoriteMixes.size}: $displayTitle",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentMix.pictures.size} Artists: " + currentMix.pictures.joinToString(", ") { it.fileName.substringBeforeLast('.') },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Action Buttons (Load & Delete)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onSelectMix(currentMix)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("lightbox_load_mix_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("lightbox_delete_mix_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Delete Confirmation Prompt
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = {
                        Text(
                            text = "Delete Mix",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete '$displayTitle'?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmation = false
                                val idToDelete = currentMix.id
                                onDeleteMix(idToDelete)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

private fun String?.isNullByBlank(): Boolean = this.isNullOrBlank()
