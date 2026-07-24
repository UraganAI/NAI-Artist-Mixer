package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.ui.components.FavoritePreviewDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.components.FolderSelector
import com.example.ui.components.FormattingPanel
import com.example.ui.components.MatchArtistDialog
import com.example.ui.components.PictureGrid
import com.example.ui.components.ResultCard
import com.example.ui.components.ThemeSelectorDialog
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val appThemePreset by viewModel.appThemePreset.collectAsStateWithLifecycle()
    val folderName by viewModel.folderName.collectAsStateWithLifecycle()
    val availablePictures by viewModel.availablePictures.collectAsStateWithLifecycle()
    val requestedCount by viewModel.requestedCount.collectAsStateWithLifecycle()
    val allowDuplicates by viewModel.allowDuplicates.collectAsStateWithLifecycle()
    val formattingOptions by viewModel.formattingOptions.collectAsStateWithLifecycle()
    val currentResult by viewModel.currentResult.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val showFirstStartPrompt by viewModel.showFirstStartPrompt.collectAsStateWithLifecycle()
    val favoriteArtistNames by viewModel.favoriteArtistNames.collectAsStateWithLifecycle()

    val customPrimaryArgb by viewModel.customPrimaryColorArgb.collectAsStateWithLifecycle()
    val customBgArgb by viewModel.customBgColorArgb.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var showMatchArtistDialog by remember { mutableStateOf(false) }
    var previewFavoriteIndex by remember { mutableIntStateOf(-1) }

    val lockedCount = currentResult?.selectedPictures?.count { it.isLocked } ?: 0
    val canDecrement = requestedCount > 1 && requestedCount > lockedCount

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.loadFromTreeUri(context, uri)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.loadFromMultipleUris(context, uris)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val jsonString = viewModel.exportDataJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(jsonString.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { isStream ->
                    val jsonString = isStream.bufferedReader().readText()
                    viewModel.importDataFromJson(jsonString)
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NAI Artist Mixer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Artist Mixing tool for NovelAI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Match your Artist (Tinder sub-menu)
                    IconButton(
                        onClick = { showMatchArtistDialog = true },
                        modifier = Modifier.testTag("match_artist_button")
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_match_artist),
                            contentDescription = "Match your Artist",
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Palette icon left of favorite submenu for custom color theme
                    IconButton(
                        onClick = { showThemeSelectorDialog = true },
                        modifier = Modifier.testTag("theme_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Theme Selector",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorite artists submenu button
                    IconButton(
                        onClick = { showFavoritesDialog = true },
                        modifier = Modifier.testTag("favorites_button")
                    ) {
                        Icon(
                            imageVector = if (favoriteArtistNames.isNotEmpty()) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Artists Submenu",
                            tint = if (favoriteArtistNames.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // About App Info
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. FORMATTED RESULT CARD
            ResultCard(
                result = currentResult,
                onPickAgain = { viewModel.pickRandomPictures() },
                onCopy = { viewModel.copyResultToClipboard(context, it) }
            )

            // 2. SELECTED ARTISTS VISUAL GRID
            PictureGrid(
                pictures = currentResult?.selectedPictures ?: emptyList(),
                availablePictures = availablePictures,
                favoriteArtistNames = favoriteArtistNames,
                viewModel = viewModel,
                onRerollItem = { viewModel.rerollSinglePicture(it) },
                onToggleLock = { viewModel.toggleLockPicture(it) },
                onToggleFavorite = { viewModel.toggleFavoriteArtist(it) },
                onAddManualPicture = { viewModel.addManualPicture(it) },
                onMoveItem = { from, to -> viewModel.movePicture(from, to) }
            )

            // 3. FOLDER SELECTOR
            FolderSelector(
                currentFolderName = folderName,
                totalCount = availablePictures.size,
                onSelectTreeUri = { viewModel.loadFromTreeUri(context, it) },
                onSelectMultipleUris = { viewModel.loadFromMultipleUris(context, it) },
                onSelectNaxMoeLooseGallery = { viewModel.loadNaxMoeLooseGallery() },
                onSelectNaxMoeConstrainedGallery = { viewModel.loadNaxMoeConstrainedGallery() }
            )

            // 4. FORMATTING & SELECTION CONTROLS
            val isNaxMoe = folderName.contains("nax.moe", ignoreCase = true) || 
                           folderName.contains("danbooru", ignoreCase = true) || 
                           folderName.contains("prompt", ignoreCase = true)
            FormattingPanel(
                requestedCount = requestedCount,
                totalAvailable = availablePictures.size,
                allowDuplicates = allowDuplicates,
                formattingOptions = formattingOptions,
                isNaxMoe = isNaxMoe,
                canDecrement = canDecrement,
                onCountChange = { viewModel.setRequestedCount(it) },
                onAllowDuplicatesChange = { viewModel.setAllowDuplicates(it) },
                onFormattingOptionsChange = { viewModel.updateFormattingOptions(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Color Theme Selector Submenu Dialog
    if (showThemeSelectorDialog) {
        ThemeSelectorDialog(
            currentTheme = appThemePreset,
            customPrimaryArgb = customPrimaryArgb,
            customBgArgb = customBgArgb,
            onSelectTheme = { viewModel.setAppThemePreset(it) },
            onSetCustomColors = { primary, bg -> viewModel.setCustomColors(primary, bg) },
            onDismiss = { showThemeSelectorDialog = false }
        )
    }

    // Match your Artist Tinder-style Submenu Dialog
    if (showMatchArtistDialog) {
        MatchArtistDialog(
            viewModel = viewModel,
            onDismiss = { showMatchArtistDialog = false }
        )
    }

    // Favorite Artists Submenu Dialog
    val sortedFavorites = remember(favoriteArtistNames) { favoriteArtistNames.toList().sorted() }

    if (previewFavoriteIndex in sortedFavorites.indices) {
        FavoritePreviewDialog(
            favoritesList = sortedFavorites,
            initialIndex = previewFavoriteIndex,
            availablePictures = availablePictures,
            viewModel = viewModel,
            onDismiss = { previewFavoriteIndex = -1 }
        )
    }

    if (showFavoritesDialog) {
        var favToastText by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(toastMessage) {
            toastMessage?.let { msg ->
                if (msg.contains("added to selection", ignoreCase = true)) {
                    favToastText = msg
                }
            }
        }
        LaunchedEffect(favToastText) {
            if (favToastText != null) {
                kotlinx.coroutines.delay(2200)
                favToastText = null
            }
        }

        Dialog(onDismissRequest = { showFavoritesDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FAVORITE ARTISTS (${favoriteArtistNames.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(onClick = { showFavoritesDialog = false }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap a picture to preview or swipe. Tap name to add to selection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (favoriteArtistNames.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No favorite artists added yet.\nTap the heart icon on any picture or use Match your Artist to add!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(sortedFavorites) { index, artistName ->
                                val matchingPic = availablePictures.firstOrNull {
                                    it.fileName.equals(artistName, ignoreCase = true)
                                }
                                val isPresent = matchingPic != null

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isPresent)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left Thumbnail / Icon
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .then(
                                                    if (isPresent) Modifier.clickable {
                                                        previewFavoriteIndex = index
                                                    } else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isPresent && matchingPic != null) {
                                                if (matchingPic.drawableResId != null) {
                                                    Image(
                                                        painter = painterResource(id = matchingPic.drawableResId),
                                                        contentDescription = artistName,
                                                        contentScale = ContentScale.Crop,
                                                        alignment = Alignment.TopCenter,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else if (matchingPic.uriString.isNotBlank()) {
                                                    SubcomposeAsyncImage(
                                                        model = ImageRequest.Builder(context)
                                                            .data(matchingPic.uriString)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = artistName,
                                                        contentScale = ContentScale.Crop,
                                                        alignment = Alignment.TopCenter,
                                                        loading = {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                        },
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.BrokenImage,
                                                    contentDescription = "Absent",
                                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Artist Name & Status
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = isPresent) {
                                                    viewModel.addFavoriteToSelection(artistName)
                                                }
                                        ) {
                                            Text(
                                                text = artistName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = if (isPresent)
                                                    MaterialTheme.colorScheme.onSurface
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (!isPresent) {
                                                Text(
                                                    text = "artist absent from database",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Delete Button
                                        IconButton(
                                            onClick = { viewModel.toggleFavoriteArtist(artistName) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove favorite",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Toast Window
                AnimatedVisibility(
                    visible = favToastText != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = favToastText ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Prompt for Image Folder on First Startup
    if (showFirstStartPrompt && availablePictures.isEmpty()) {
        Dialog(onDismissRequest = { viewModel.dismissFirstStartPrompt() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select Image Source",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To start picking random artists or photos, select an online Danbooru gallery or choose a folder on your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            viewModel.loadNaxMoeLooseGallery()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loose Prompt (nax.moe)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            viewModel.loadNaxMoeConstrainedGallery()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Constrained Prompt (nax.moe)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            folderPickerLauncher.launch(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Folder", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            filePickerLauncher.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Specific Photos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // About NAI Artist Mixer Dialog
    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "About NAI Artist Mixer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NAI Artist Mixer randomly selects Danbooru artists (from nax.moe) or photos from designated folders and formats their names into customizable prompt lists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Easily preview artist styles, lock your favorite picks, re-roll individual slots, and discover new aesthetics with the built-in 'Match your Artist' card deck.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Special thanks to nax.moe for providing the Danbooru artist tag galleries, and to the Anlatan team, for making NovelAI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetPassedArtists() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reset_passed_artists_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Match your Artist Passed Swipes",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Data Backup & Restore",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val jsonStr = viewModel.exportDataJson()
                                viewModel.copyResultToClipboard(context, jsonStr)
                                exportLauncher.launch("nai_artist_mixer_backup.json")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("export_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export Data",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                importLauncher.launch("application/json")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("import_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Import Data",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/UraganAI/NAI-Artist-Mixer"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_link_button")
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_github),
                            contentDescription = "GitHub Repository",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub Repository",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "This application was made with AI. Prompted by Uragan.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showInfoDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Got It")
                    }
                }
            }
        }
    }
}
